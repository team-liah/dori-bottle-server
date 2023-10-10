package com.liah.doribottle.service.payment

import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.KAKAOPAY
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.TOSS_PAYMENTS
import com.liah.doribottle.service.payment.client.tosspayments.TosspaymentsApiClient
import org.springframework.stereotype.Service
import java.util.*

@Service
class PaymentGatewayService(
    private val tosspaymentsApiClient: TosspaymentsApiClient
) {
    fun issuePaymentKey(
        pgType: PaymentMethodProviderType,
        userId: UUID,
        authKey: String? = null
    ) {
        when(pgType) {
            TOSS_PAYMENTS -> {
                authKey ?: throw IllegalArgumentException()
                tosspaymentsApiClient.issueBillingKey(
                    authKey = authKey,
                    customerKey = userId.toString()
                )
            }
            KAKAOPAY -> {

            }
        }
    }

    fun pay(

    ) {

    }

    fun cancelPayment(

    ) {

    }
}