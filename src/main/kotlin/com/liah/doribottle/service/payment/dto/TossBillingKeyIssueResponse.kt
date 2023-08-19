package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.extension.findBy
import java.time.Instant

data class TossBillingKeyIssueResponse(
    val mId: String,
    val customerKey: String,
    val authenticatedAt: Instant,
    val method: String,
    val billingKey: String,
    val card: CardResponse
) {
    fun toBillingInfo() = BillingInfo(
        billingKey = billingKey,
        providerType = PaymentMethodProviderType.TOSS_PAYMENTS,
        type = (PaymentMethodType::title findBy method)!!,
        cardDto = card.toDto(),
        authenticatedDate = authenticatedAt
    )
}

data class CardResponse(
    val issuerCode: String,
    val acquirerCode: String,
    val number: String,
    val cardType: String,
    val ownerType: String,
) {
    fun toDto() = CardDto(
        issuerProvider = (CardProvider::code findBy issuerCode)!!,
        acquirerProvider = (CardProvider::code findBy acquirerCode)!!,
        number = number,
        cardType = (CardType::title findBy cardType)!!,
        cardOwnerType = (CardOwnerType::title findBy ownerType)!!
    )
}