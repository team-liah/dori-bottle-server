package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.error.exception.*
import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.web.v1.payment.vm.*
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
    private val tossPaymentsService: TossPaymentsService,
    private val pointService: PointService
) {
    @PostMapping("/save-point")
    fun payToSavePoint(
        @Valid @RequestBody request: PayToSavePointRequest
    ): UUID {
        val currentUserId = currentUserId()!!
        val category = paymentService.getCategory(request.categoryId!!)
        val price = category.getFinalPrice()
        val method = paymentService.getDefaultMethod(currentUserId)
        val id = paymentService.create(
            userId = currentUserId,
            price = price,
            type = PaymentType.SAVE_POINT,
            card = method.card
        )

        runCatching {
            tossPaymentsService.executeBilling(
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

    @GetMapping
    fun getAll(
        @RequestParam(value = "type", required = false) type: PaymentType?,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PaymentSearchResponse> {
        val result = paymentService.getAll(
            userId = currentUserId()!!,
            type = type,
            statuses = setOf(PaymentStatus.SUCCEEDED, PaymentStatus.CANCELED),
            pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

    @PostMapping("/{id}/cancel")
    fun cancelPayment(
        @PathVariable id: UUID
    ) {
        val payment = paymentService.get(id)

        if (payment.userId != currentUserId()) throw ForbiddenException()
        if (payment.type != PaymentType.SAVE_POINT) throw BusinessException(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED)
        val paymentResult = payment.result ?: throw NotFoundException(ErrorCode.PAYMENT_NOT_FOUND)

        runCatching {
            tossPaymentsService.cancelPayment(
                paymentKey = paymentResult.paymentKey,
                cancelReason = "포인트 적립 취소"
            )
        }.onSuccess { result ->
            paymentService.updateResult(
                id = id,
                result = result
            )
        }.onFailure {
            throw PaymentCancelException()
        }
    }

    @PostMapping("/method")
    fun registerMethod(
        @Valid @RequestBody request: PaymentMethodRegisterRequest
    ): UUID {
        val billingInfo = when (request.providerType!!) {
            PaymentMethodProviderType.TOSS_PAYMENTS -> {
                tossPaymentsService.issueBillingKey(
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

    @PostMapping("/method/{id}/default")
    fun changeDefaultMethod(
        @PathVariable id: UUID
    ) {
        paymentService.changeDefaultMethod(
            id = id,
            userId = currentUserId()!!
        )
    }

    @DeleteMapping("/method/{id}")
    fun removeMethod(
        @PathVariable id: UUID
    ) {
        val method = paymentService.getMethod(id)
        if (method.userId != currentUserId()) throw ForbiddenException()

        paymentService.removeMethod(id)
    }

    @GetMapping("/category")
    fun getAllCategories(
        @ParameterObject @PageableDefault(sort = ["amounts"], direction = Sort.Direction.ASC) pageable: Pageable
    ): CustomPage<PaymentCategorySearchResponse> {
        val result = paymentService.getAllCategories(
            expired = false,
            deleted = false,
            pageable = pageable
        ).map { it.toUserResponse() }

        return CustomPage.of(result)
    }
}