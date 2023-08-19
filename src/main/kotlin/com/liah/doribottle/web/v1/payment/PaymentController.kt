package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.web.v1.payment.vm.PaymentCategorySearchResponse
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodRegisterRequest
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payment")
class PaymentController(
    private val paymentService: PaymentService,
    private val tossPaymentsService: TossPaymentsService
) {
    @PostMapping("/method")
    fun registerMethod(
        @Valid @RequestBody request: PaymentMethodRegisterRequest
    ) {
        val billingInfo = when (request.providerType!!) {
            PaymentMethodProviderType.TOSS_PAYMENTS -> {
                tossPaymentsService.issueBillingKey(
                    authKey = request.authKey!!,
                    userId = currentUserId()!!
                )
            }
        }
    }

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