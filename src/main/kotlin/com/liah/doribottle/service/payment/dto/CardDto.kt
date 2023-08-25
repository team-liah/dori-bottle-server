package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.web.v1.payment.vm.CardResponse

data class CardDto(
    val issuerProvider: CardProvider,
    val acquirerProvider: CardProvider,
    val number: String,
    val cardType: CardType,
    val cardOwnerType: CardOwnerType
) {
    fun toEmbeddable() = Card(issuerProvider, acquirerProvider, number, cardType, cardOwnerType)
    fun toResponse() = CardResponse(acquirerProvider.title, number.substring(number.length-4, number.length), cardType.title, cardOwnerType.title)
}