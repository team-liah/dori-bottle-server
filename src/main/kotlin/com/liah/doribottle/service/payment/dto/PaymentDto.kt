package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import java.util.UUID

data class PaymentDto(
    val id: UUID,
    val userId: UUID,
    val price: Long,
    val type: PaymentType,
    val card: CardDto,
    val status: PaymentStatus,
    val result: PaymentResultDto?,
    val pointId: UUID?
)