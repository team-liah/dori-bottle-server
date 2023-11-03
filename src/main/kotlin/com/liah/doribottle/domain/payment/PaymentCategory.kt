package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.service.payment.dto.PaymentCategoryDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "payment_category")
class PaymentCategory(
    amounts: Long,
    price: Long,
    discountRate: Int,
    discountExpiredDate: Instant?,
    expiredDate: Instant?
) : SoftDeleteEntity() {
    @Column(nullable = false)
    val amounts: Long = amounts

    @Column(nullable = false)
    val price: Long = price

    @Column(nullable = false)
    val discountRate: Int = discountRate

    @Column
    val discountExpiredDate: Instant? = discountExpiredDate

    @Column
    val expiredDate: Instant? = expiredDate

    fun toDto() = PaymentCategoryDto(id, amounts, price, discountRate, discountExpiredDate, expiredDate, deleted, createdDate, lastModifiedDate)
}