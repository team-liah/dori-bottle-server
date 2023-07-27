package com.liah.doribottle.service.payment.dto

import java.time.Instant
import java.util.UUID

data class PaymentCategoryDto(
    val id: UUID,
    val amounts: Long,
    val price: Long,
    val discountRate: Int?,
    val discountExpiredDate: Instant?,
    val expiredDate: Instant?,
    val deleted: Boolean
)