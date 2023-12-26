package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.error.exception.*
import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.group.GroupService
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TosspaymentsService
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.v1.payment.vm.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val tosspaymentsService: TosspaymentsService,
    private val pointService: PointService,
    private val userService: UserService,
    private val groupService: GroupService
) {
    @Operation(summary = "결제 - 포인트 충전")
    @PostMapping("/save-point")
    fun payToSavePoint(
        @Valid @RequestBody request: PayToSavePointRequest
    ): UUID {
        val currentUserId = currentUserId()!!
        val currentUserGroup = groupService.findByUserId(currentUserId)
        val category = paymentService.getCategory(request.categoryId!!)
        val price = category.getFinalPrice(currentUserGroup?.discountRate)
        val method = paymentService.getDefaultMethod(currentUserId)
        val id = paymentService.create(
            userId = currentUserId,
            price = price,
            type = PaymentType.SAVE_POINT,
            card = method.card
        )

        runCatching {
            tosspaymentsService.executeBilling(
                billingKey = method.billingKey,
                userId = currentUserId,
                price = price,
                paymentId = id,
                paymentType = PaymentType.SAVE_POINT
            )
        }.onSuccess { result ->
            val pointId = pointService.save(
                userId = currentUserId,
                saveType = PointSaveType.PAY,
                eventType = PointEventType.SAVE_PAY,
                saveAmounts = category.amounts
            )
            paymentService.updateResult(
                id = id,
                result = result,
                pointId = pointId
            )
        }.onFailure {
            paymentService.updateResult(
                id = id,
                result = null
            )
            throw BillingExecuteException()
        }

        return id
    }

    @Operation(summary = "결제 - 계정 블락 해제")
    @PostMapping("/unblock-account")
    fun payToUnblockAccount(): UUID {
        val currentUserId = currentUserId()!!
        val blockedCauses = userService.get(currentUserId).blockedCauses
        val price = blockedCauses.sumOf { it.clearPrice }

        if (blockedCauses.isEmpty()) throw BusinessException(ErrorCode.UNBLOCKED_USER)

        val method = paymentService.getDefaultMethod(currentUserId)
        val id = paymentService.create(
            userId = currentUserId,
            price = price,
            type = PaymentType.UNBLOCK_ACCOUNT,
            card = method.card
        )

        runCatching {
            tosspaymentsService.executeBilling(
                billingKey = method.billingKey,
                userId = currentUserId,
                price = price,
                paymentId = id,
                paymentType = PaymentType.UNBLOCK_ACCOUNT
            )
        }.onSuccess { result ->
            userService.unblock(
                id = currentUserId,
                blockedCauseIds = blockedCauses.map { it.id }.toSet()
            )
            paymentService.updateResult(
                id = id,
                result = result
            )
        }.onFailure {
            paymentService.updateResult(
                id = id,
                result = null
            )
            throw BillingExecuteException()
        }

        return id
    }

    @Operation(summary = "결제내역 조회")
    @GetMapping
    fun getAll(
        @RequestParam(value = "type", required = false) type: PaymentType?,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PaymentSearchResponse> {
        val result = paymentService.getAll(
            userId = currentUserId()!!,
            type = type,
            statuses = setOf(PaymentStatus.SUCCEEDED, PaymentStatus.CANCELED),
            pageable = pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

    @Operation(summary = "결제 취소")
    @PostMapping("/{id}/cancel")
    fun cancel(
        @PathVariable id: UUID
    ) {
        val payment = paymentService.get(id)

        if (payment.user.id != currentUserId()) throw ForbiddenException()
        if (payment.type != PaymentType.SAVE_POINT) throw BusinessException(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED)
        if (payment.point!!.saveAmounts != payment.point.remainAmounts) throw BusinessException(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED)
        val paymentResult = payment.result ?: throw NotFoundException(ErrorCode.PAYMENT_NOT_FOUND)

        runCatching {
            tosspaymentsService.cancelPayment(
                paymentKey = paymentResult.paymentKey,
                cancelReason = "포인트 적립 취소"
            )
        }.onSuccess { result ->
            paymentService.updateResult(
                id = id,
                result = result,
                pointId = payment.point.id
            )
        }.onFailure {
            throw PaymentCancelException()
        }
    }

    @Operation(summary = "결제수단 등록")
    @PostMapping("/method")
    fun registerMethod(
        @Valid @RequestBody request: PaymentMethodRegisterRequest
    ): UUID {
        val billingInfo = when (request.providerType!!) {
            PaymentMethodProviderType.TOSSPAYMENTS -> {
                tosspaymentsService.issueBillingKey(
                    authKey = request.authKey!!,
                    userId = currentUserId()!!
                )
            }
        }

        return paymentService.registerMethod(
            userId = currentUserId()!!,
            billingInfo = billingInfo
        )
    }

    @Operation(summary = "결제수단 목록 조회")
    @GetMapping("/method")
    fun getAllMethods(
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PaymentMethodSearchResponse> {
        val result = paymentService.getAllMethods(
            userId = currentUserId()!!,
            pageable = pageable
        ).map { it.toResponse() }

        return CustomPage.of(result)
    }

    @Operation(summary = "기본 결제수단 변경")
    @PostMapping("/method/{id}/default")
    fun changeDefaultMethod(
        @PathVariable id: UUID
    ) {
        paymentService.changeDefaultMethod(
            id = id,
            userId = currentUserId()!!
        )
    }

    @Operation(summary = "결제수단 제거")
    @DeleteMapping("/method/{id}")
    fun removeMethod(
        @PathVariable id: UUID
    ) {
        val method = paymentService.getMethod(id)
        if (method.userId != currentUserId()) throw ForbiddenException()

        paymentService.removeMethod(id)
    }

    @Operation(summary = "결제 카테고리 목록 조회")
    @GetMapping("/category")
    fun getAllCategories(
        @ParameterObject @PageableDefault(sort = ["amounts"], direction = Sort.Direction.ASC) pageable: Pageable
    ): CustomPage<PaymentCategorySearchResponse> {
        val currentUserGroup = groupService.findByUserId(currentUserId()!!)
        val result = paymentService.getAllCategories(
            expired = false,
            pageable = pageable
        ).map { it.toUserResponse(currentUserGroup?.discountRate) }

        return CustomPage.of(result)
    }
}