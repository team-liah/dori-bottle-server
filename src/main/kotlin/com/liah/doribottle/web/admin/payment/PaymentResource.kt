package com.liah.doribottle.web.admin.payment

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.web.admin.payment.vm.PaymentCategoryRegisterOrUpdateRequest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchRequest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchResponse
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
    private val paymentService: PaymentService
) {
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