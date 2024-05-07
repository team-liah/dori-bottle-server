package com.liah.doribottle.apiclient.vm

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.extension.findBy
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.CardDto
import java.time.Instant

data class TosspaymentsBillingKeyIssueResponse(
    val mId: String,
    val customerKey: String,
    val authenticatedAt: Instant,
    val method: String,
    val billingKey: String,
    val card: TosspaymentsBillingCardResponse,
) {
    fun toBillingInfo() =
        BillingInfo(
            billingKey = billingKey,
            providerType = PaymentMethodProviderType.TOSS_PAYMENTS,
            type = (PaymentMethodType::title findBy method)!!,
            cardDto = card.toDto(),
            authenticatedDate = authenticatedAt,
        )
}

data class TosspaymentsBillingCardResponse(
    val issuerCode: String,
    val acquirerCode: String,
    val number: String,
    val cardType: String,
    val ownerType: String,
) {
    fun toDto() =
        CardDto(
            issuerProvider = (CardProvider::code findBy issuerCode)!!,
            acquirerProvider = (CardProvider::code findBy acquirerCode)!!,
            number = number,
            cardType = (CardType::title findBy cardType)!!,
            cardOwnerType = (CardOwnerType::title findBy ownerType)!!,
        )
}
