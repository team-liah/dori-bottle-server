package com.liah.doribottle.web.admin.payment.vm

import java.time.Instant
import java.util.*

data class PaymentCategorySearchResponse(
    val id: UUID,
    val amounts: Long,
    val price: Long,
    val discountRate: Int,
    val discountPrice: Long,
    val discountExpiredDate: Instant?,
    val expiredDate: Instant?
)