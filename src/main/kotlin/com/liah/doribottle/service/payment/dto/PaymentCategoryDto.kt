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
    fun getFinalPrice(
        initDiscountRate: Int?
    ): Long {
        val now = Instant.now()
        val discountExpired = discountExpiredDate != null && discountExpiredDate.isBefore(now)
        val discountRate = if (discountExpired) 0 else this.discountRate
        return getDiscountPrice(
            price = getDiscountPrice(
                price = price,
                discountRate = initDiscountRate ?: 0
            ),
            discountRate = discountRate
        )
    }

    fun toUserResponse(
        initDiscountRate: Int?
    ): PaymentCategorySearchResponse {
        val now = Instant.now()
        val discountExpired = discountExpiredDate != null && discountExpiredDate.isBefore(now)
        val discountRate = if (discountExpired) 0 else this.discountRate
        val discountPrice = getDiscountPrice(
            price = getDiscountPrice(
                price = price,
                discountRate = initDiscountRate ?: 0
            ),
            discountRate = discountRate
        )
        return PaymentCategorySearchResponse(id, amounts, price, discountRate, discountPrice, discountExpiredDate, expiredDate)
    }
    fun toAdminResponse() = com.liah.doribottle.web.admin.payment.vm.PaymentCategorySearchResponse(id, amounts, price, discountRate, getDiscountPrice(price, discountRate), discountExpiredDate, expiredDate, deleted)
    private fun getDiscountPrice(price: Long, discountRate: Int) = price-(price*discountRate/100)
}