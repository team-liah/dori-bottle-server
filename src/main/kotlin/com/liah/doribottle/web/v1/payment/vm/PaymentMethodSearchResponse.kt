package com.liah.doribottle.web.v1.payment.vm

import com.liah.doribottle.domain.payment.PaymentMethodType
import java.util.*

data class PaymentMethodSearchResponse(
    val id: UUID,
    val type: PaymentMethodType,
    val card: CardResponse,
    val default: Boolean
)