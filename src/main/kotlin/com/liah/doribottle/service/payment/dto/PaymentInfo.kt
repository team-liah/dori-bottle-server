package com.liah.doribottle.service.payment.dto

import java.time.Instant

data class PaymentInfo(
    val paymentKey: String,
    val approvedDate: Instant,
    val receiptUrl: String?
)