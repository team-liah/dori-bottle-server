package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.PaymentType.SAVE_POINT
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.payment.dto.PaymentDto
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(
    name = "payment",
    indexes = [Index(name = "IDX_PAYMENT_USER_ID", columnList = "user_id")],
)
class Payment(
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @Column(nullable = false)
    val price: Long,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PaymentType,
    @Embedded
    val card: Card,
) : PrimaryKeyEntity() {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.PROCEEDING
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
        point: Point?,
    ) {
        val status =
            if (result == null) {
                PaymentStatus.FAILED
            } else if (result.cancelKey != null) {
                PaymentStatus.CANCELED
            } else {
                PaymentStatus.SUCCEEDED
            }

        this.result = result
        this.point = point
        this.status = status

        verifyResult()
    }

    private fun verifyResult() {
        if ((type == SAVE_POINT) && (status == PaymentStatus.SUCCEEDED || status == PaymentStatus.CANCELED) && (point == null)) {
            throw IllegalArgumentException("Null point is not allowed if payment type is SAVE_POINT")
        }
        if (type != SAVE_POINT && point != null) {
            throw IllegalArgumentException("Point is not allowed if payment type is not SAVE_POINT")
        }
    }

    fun toDto() =
        PaymentDto(
            id = id,
            user = user.toSimpleDto(),
            price = price,
            type = type,
            card = card.toDto(),
            status = status,
            result = result?.toDto(),
            point = point?.toDto(),
            createdDate = createdDate,
            lastModifiedDate = lastModifiedDate,
        )
}
