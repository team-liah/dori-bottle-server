package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.service.payment.dto.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class TossPaymentsApiClient(
    @Value("\${app.toss.payments.baseUrl}") private val baseUrl: String,
    @Value("\${app.toss.payments.secretKey}") private val secretKey: String
) {
    private val billingKeyIssueRequestUri = "${baseUrl}/v1/billing/authorizations/issue"
    private fun billingExecuteRequestUri(billingKey: String) = "${baseUrl}/v1/billing/${billingKey}"
    private fun paymentCancelRequestUri(paymentKey: String) = "${baseUrl}/v1/payments/${paymentKey}/cancel"

    fun issueBillingKey(
        authKey: String,
        customerKey: String
    ): TossBillingKeyIssueResponse? {
        val request = TossBillingKeyIssueRequest(
            authKey = authKey,
            customerKey = customerKey
        )

        return WebClient.create()
            .post()
            .uri(billingKeyIssueRequestUri)
            .headers { header -> header.setBasicAuth(secretKey) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TossBillingKeyIssueResponse::class.java)
            .doOnError { throw BillingKeyIssuanceException() }
            .block()
    }

    fun executeBilling(
        billingKey: String,
        customerKey: String,
        amount: Long,
        orderId: String,
        orderName: String
    ): TossPaymentResponse? {
        val request = TossBillingExecuteRequest(
            customerKey = customerKey,
            amount = amount,
            orderId = orderId,
            orderName = orderName
        )

        return WebClient.create()
            .post()
            .uri(billingExecuteRequestUri(billingKey))
            .headers { header -> header.setBasicAuth(secretKey) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TossPaymentResponse::class.java)
            .doOnError { throw BillingExecuteException() }
            .block()
    }

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): TossPaymentResponse? {
        val request = TossPaymentCancelRequest(
            cancelReason = cancelReason
        )

        return WebClient.create()
            .post()
            .uri(paymentCancelRequestUri(paymentKey))
            .headers { header -> header.setBasicAuth(secretKey) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TossPaymentResponse::class.java)
            .doOnError { throw PaymentCancelException() }
            .block()
    }
}