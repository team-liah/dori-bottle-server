package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.PaymentStatus.*
import com.liah.doribottle.domain.payment.PaymentType.SAVE_POINT
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.user.User
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.util.*

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

    @Column
    var pointId: UUID? = null
        protected set

    fun updateResult(
        result: PaymentResult?,
        pointId: UUID?
    ) {
        if (result == null) {
            this.status = FAILED
        } else {
            this.result = result
            this.pointId = pointId
            this.status = SUCCEEDED

            verifyType()
        }
    }

    private fun verifyType() {
        if (type == SAVE_POINT && pointId == null)
            throw IllegalArgumentException("Null pointId is not allowed if payment type is SAVE_POINT")
        if (type != SAVE_POINT && pointId != null)
            throw IllegalArgumentException("PointId is not allowed if payment type is not SAVE_POINT")
    }
}