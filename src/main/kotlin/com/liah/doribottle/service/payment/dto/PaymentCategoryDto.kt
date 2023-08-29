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
    fun getFinalPrice(): Long {
        val now = Instant.now()
        val discountExpired = discountExpiredDate != null && discountExpiredDate.isBefore(now)

        return if (discountExpired) {
            price
        } else {
            price-(price*discountRate/100)
        }
    }
    fun toUserResponse(): PaymentCategorySearchResponse {
        val now = Instant.now()
        val discountExpired = discountExpiredDate != null && discountExpiredDate.isBefore(now)
        val discountRate = if (discountExpired) 0 else this.discountRate
        return PaymentCategorySearchResponse(id, amounts, price, discountRate, price-(price*discountRate/100), discountExpiredDate, expiredDate)
    }
    fun toAdminResponse() = com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchResponse(id, amounts, price, discountRate, price-(price*discountRate/100), discountExpiredDate, expiredDate, deleted)
}