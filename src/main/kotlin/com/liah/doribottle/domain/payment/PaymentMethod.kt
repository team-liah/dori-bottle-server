package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.user.User
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.time.Instant

@Entity
@Table(name = "payment_method")
class PaymentMethod(
    user: User,
    providerType: PaymentMethodProviderType,
    billingKey: String,
    type: PaymentMethodType,
    card: Card,
    authenticatedDate: Instant,
    default: Boolean
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

    @Column(nullable = false)
    val authenticatedDate: Instant = authenticatedDate

    @Embedded
    val card: Card = card

    @Column(nullable = false)
    var default: Boolean = default
        protected set
}