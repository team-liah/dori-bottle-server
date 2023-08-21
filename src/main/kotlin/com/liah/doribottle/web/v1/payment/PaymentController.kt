package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.error.exception.ForbiddenException
import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.web.v1.payment.vm.PaymentCategorySearchResponse
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodRegisterRequest
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodSearchResponse
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
    private val tossPaymentsService: TossPaymentsService
) {
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