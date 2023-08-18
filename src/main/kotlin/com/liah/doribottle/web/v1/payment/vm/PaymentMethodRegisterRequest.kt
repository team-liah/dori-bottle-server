package com.liah.doribottle.web.v1.payment.vm

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import org.jetbrains.annotations.NotNull

data class PaymentMethodRegisterRequest(
    @field:NotNull
    val providerType: PaymentMethodProviderType?,
    @field:NotNull
    val customerKey: String?,
    @field:NotNull
    val authKey: String?
)