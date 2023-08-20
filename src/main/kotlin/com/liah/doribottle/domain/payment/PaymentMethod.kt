package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.payment.dto.PaymentMethodDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.time.Instant

@Entity
@Table(name = "payment_method")
class PaymentMethod(
    user: User,
    billingKey: String,
    providerType: PaymentMethodProviderType,
    type: PaymentMethodType,
    card: Card,
    default: Boolean,
    authenticatedDate: Instant
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = user

    @Column(nullable = false, unique = true)
    val billingKey: String = billingKey

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val providerType: PaymentMethodProviderType = providerType

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PaymentMethodType = type

    @Embedded
    val card: Card = card

    @Column(name = "`default`", nullable = false)
    var default: Boolean = default
        protected set

    @Column(nullable = false)
    val authenticatedDate: Instant = authenticatedDate

    fun update(default: Boolean) {
        this.default = default
    }

    fun toDto() = PaymentMethodDto(id, user.id, billingKey, providerType, type, card.toDto(), default, authenticatedDate)
}