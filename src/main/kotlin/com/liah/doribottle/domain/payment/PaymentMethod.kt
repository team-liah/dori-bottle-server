package com.liah.doribottle.domain.payment

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.payment.dto.PaymentMethodDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.time.Instant

@Entity
@Table(
    name = "payment_method",
    indexes = [Index(name = "IDX_PAYMENT_METHOD_USER_ID", columnList = "user_id")]
)
class PaymentMethod(
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false, unique = true)
    val billingKey: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val providerType: PaymentMethodProviderType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PaymentMethodType,

    @Embedded
    val card: Card,

    default: Boolean,

    @Column(nullable = false)
    val authenticatedDate: Instant
) : PrimaryKeyEntity() {
    @Column(name = "`default`", nullable = false)
    var default: Boolean = default
        protected set

    fun update(default: Boolean) {
        this.default = default
    }

    fun toDto() = PaymentMethodDto(id, user.id, billingKey, providerType, type, card.toDto(), default, authenticatedDate, createdDate, lastModifiedDate)
}