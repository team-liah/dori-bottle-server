package com.liah.doribottle.domain.payment

import com.liah.doribottle.service.payment.dto.PaymentResultDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.time.Instant

@Embeddable
data class PaymentResult(
    @Column
    val paymentKey: String,

    @Column
    val approvedDate: Instant,

    @Column
    val receiptUrl: String?,

    @Column
    val cancelKey: String?
) {
    fun toDto() = PaymentResultDto(paymentKey, approvedDate, receiptUrl, cancelKey)
}