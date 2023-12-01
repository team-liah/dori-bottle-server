package com.liah.doribottle.web.admin.payment

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.payment.dto.PaymentDto
import com.liah.doribottle.web.admin.payment.vm.PaymentCategoryRegisterOrUpdateRequest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchRequest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchResponse
import com.liah.doribottle.web.admin.payment.vm.PaymentSearchRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/payment")
class PaymentResource(
    private val paymentService: PaymentService,
    private val tossPaymentsService: TossPaymentsService
) {
    @Operation(summary = "유저 결제내역 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: PaymentSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PaymentDto> {
        val result = paymentService.getAll(
            userId = request.userId,
            type = request.type,
            statuses = request.status?.let { setOf(it) },
            pageable
        )

        return CustomPage.of(result)
    }

    // TODO: TEST
    @Operation(summary = "유저 결제내역 단건 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): PaymentDto {
        return paymentService.get(id)
    }

    // TODO: TEST
    @Operation(summary = "유저 결제 취소 처리")
    @PostMapping("/{id}/cancel")
    fun cancel(
        @PathVariable id: UUID
    ) {
        val payment = paymentService.get(id)
        val paymentResult = payment.result ?: throw NotFoundException(ErrorCode.PAYMENT_NOT_FOUND)

        runCatching {
            tossPaymentsService.cancelPayment(
                paymentKey = paymentResult.paymentKey,
                cancelReason = "포인트 적립 취소 (관리자)"
            )
        }.onSuccess { result ->
            paymentService.updateResult(
                id = id,
                result = result,
                pointId = payment.point?.id
            )
        }.onFailure {
            throw PaymentCancelException()
        }
    }

    @Operation(summary = "결제 카테고리 등록")
    @PostMapping("/category")
    fun registerCategory(
        @Valid @RequestBody request: PaymentCategoryRegisterOrUpdateRequest
    ): UUID {
        return paymentService.registerCategory(
            amounts = request.amounts!!,
            price = request.price!!,
            discountRate = request.discountRate!!,
            discountExpiredDate = request.discountExpiredDate,
            expiredDate = request.expiredDate
        )
    }

    @Operation(summary = "결제 카테고리 목록 조회")
    @GetMapping("/category")
    fun getCategories(
        @ParameterObject request: PaymentCategorySearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PaymentCategorySearchResponse> {
        val result = paymentService.getAllCategories(
            expired = request.expired,
            pageable = pageable
        ).map { it.toAdminResponse() }

        return CustomPage.of(result)
    }

    @Operation(summary = "결제 카테고리 조회")
    @GetMapping("/category/{categoryId}")
    fun getCategory(
        @PathVariable categoryId: UUID
    ): PaymentCategorySearchResponse {
        return paymentService.getCategory(categoryId).toAdminResponse()
    }

    @Operation(summary = "결제 카테고리 수정")
    @PutMapping("/category/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: UUID,
        @Valid @RequestBody request: PaymentCategoryRegisterOrUpdateRequest
    ) {
        paymentService.updateCategory(
            categoryId = categoryId,
            amounts = request.amounts!!,
            price = request.price!!,
            discountRate = request.discountRate!!,
            discountExpiredDate = request.discountExpiredDate,
            expiredDate = request.expiredDate
        )
    }

    @Operation(summary = "결제 카테고리 제거")
    @DeleteMapping("/category/{categoryId}")
    fun removeCategory(
        @PathVariable categoryId: UUID
    ) {
        paymentService.removeCategory(categoryId)
    }
}