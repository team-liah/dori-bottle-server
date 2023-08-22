package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentResult
import java.time.Instant

data class PaymentResultDto(
    val paymentKey: String,
    val approvedDate: Instant,
    val receiptUrl: String?
) {
    fun toEmbeddable() = PaymentResult(paymentKey, approvedDate, receiptUrl)
}