package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import java.time.Instant

data class BillingInfo(
    val billingKey: String,
    val providerType: PaymentMethodProviderType,
    val type: PaymentMethodType,
    val cardDto: CardDto,
    val authenticatedDate: Instant
)