package com.liah.doribottle.web.v1.payment.vm

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import java.time.Instant
import java.util.*

data class PaymentSearchResponse(
    val id: UUID,
    val userId: UUID,
    val price: Long,
    val type: PaymentType,
    val card: CardResponse,
    val status: PaymentStatus,
    val savePointAmounts: Long?,
    val remainPointAmounts: Long?,
    val createdDate: Instant
)