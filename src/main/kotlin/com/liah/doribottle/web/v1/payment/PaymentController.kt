package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.web.v1.payment.vm.PaymentCategorySearchResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(
    private val paymentService: PaymentService
) {
    @GetMapping("/category")
    fun getCategories(
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