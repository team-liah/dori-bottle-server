package com.liah.doribottle.web.admin.payment.vm

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import java.time.Instant
import java.util.*

data class PaymentSearchRequest(
    val userId: UUID?,
    val type: PaymentType?,
    val status: PaymentStatus?,
    val fromApprovedDate: Instant?,
    val toApprovedDate: Instant?
)