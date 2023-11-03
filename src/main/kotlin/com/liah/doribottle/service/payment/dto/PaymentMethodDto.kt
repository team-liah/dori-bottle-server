package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodSearchResponse
import java.time.Instant
import java.util.*

data class PaymentMethodDto(
    val id: UUID,
    val userId: UUID,
    val billingKey: String,
    val providerType: PaymentMethodProviderType,
    val type: PaymentMethodType,
    val card: CardDto,
    val default: Boolean,
    val authenticatedDate: Instant,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toResponse() = PaymentMethodSearchResponse(id, type, card.toResponse(), default)
}