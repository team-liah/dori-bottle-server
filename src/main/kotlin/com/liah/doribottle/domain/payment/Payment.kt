package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.PaymentStatus.*
import com.liah.doribottle.domain.payment.PaymentType.SAVE_POINT
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.payment.dto.PaymentDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(name = "payment")
class Payment(
    user: User,
    price: Long,
    type: PaymentType,
    card: Card
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = user

    @Column(nullable = false)
    val price: Long = price

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PaymentType = type

    @Embedded
    val card: Card = card

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PROCEEDING
        protected set

    @Embedded
    var result: PaymentResult? = null
        protected set

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "point_id")
    var point: Point? = null
        protected set

    fun updateResult(
        result: PaymentResult?,
        point: Point?
    ) {
        val status = if (result == null) {
            FAILED
        } else if (result.cancelKey != null) {
            this.point?.expire()
            this.result = result

            CANCELED
        } else {
            this.result = result
            this.point = point

            SUCCEEDED
        }

        this.status = status

        verifyResult()
    }

    private fun verifyResult() {
        if ((type == SAVE_POINT) && (status == SUCCEEDED) && (point == null))
            throw IllegalArgumentException("Null point is not allowed if payment type is SAVE_POINT")
        if (type != SAVE_POINT && point != null)
            throw IllegalArgumentException("Point is not allowed if payment type is not SAVE_POINT")
    }

    fun toDto() = PaymentDto(id, user.id, price, type, card.toDto(), status, result?.toDto(), point?.id)
}