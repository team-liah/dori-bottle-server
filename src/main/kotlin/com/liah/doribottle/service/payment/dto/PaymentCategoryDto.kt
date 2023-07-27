package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.web.v1.payment.vm.PaymentCategorySearchResponse
import java.time.Instant
import java.util.*

data class PaymentCategoryDto(
    val id: UUID,
    val amounts: Long,
    val price: Long,
    val discountRate: Int,
    val discountExpiredDate: Instant?,
    val expiredDate: Instant?,
    val deleted: Boolean
) {
    fun toUserResponse() = PaymentCategorySearchResponse(id, amounts, price, discountRate, price-(price*discountRate/100), discountExpiredDate, expiredDate)
}