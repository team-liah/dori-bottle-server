package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
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
    val authenticatedDate: Instant
)