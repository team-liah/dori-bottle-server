package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
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
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    var amounts: Long = amounts
        protected set

    @Column(nullable = false)
    var price: Long = price
        protected set

    @Column(nullable = false)
    var discountRate: Int = discountRate
        protected set

    @Column
    var discountExpiredDate: Instant? = discountExpiredDate
        protected set

    @Column
    var expiredDate: Instant? = expiredDate
        protected set

    fun update(
        amounts: Long,
        price: Long,
        discountRate: Int,
        discountExpiredDate: Instant?,
        expiredDate: Instant?
    ) {
        this.amounts = amounts
        this.price = price
        this.discountRate = discountRate
        this.discountExpiredDate = discountExpiredDate
        this.expiredDate = expiredDate
    }

    fun toDto() = PaymentCategoryDto(id, amounts, price, discountRate, discountExpiredDate, expiredDate, createdDate, lastModifiedDate)
}