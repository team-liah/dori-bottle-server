package com.liah.doribottle.domain.payment.card

import com.liah.doribottle.service.payment.dto.CardDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
data class Card(
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val issuerProvider: CardProvider,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val acquirerProvider: CardProvider,

    @Column(nullable = false)
    val number: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val cardType: CardType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val cardOwnerType: CardOwnerType
){
    fun toDto() = CardDto(issuerProvider, acquirerProvider, number, cardType, cardOwnerType)
}