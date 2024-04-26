package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.payment.dto.*
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class TosspaymentsApiClient(
    appProperties: AppProperties
) {
    private val baseUrl = appProperties.tosspayments.baseUrl
    private val secretKey = appProperties.tosspayments.secretKey
    private val billingKeyIssueRequestUri = "${baseUrl}/v1/billing/authorizations/issue"
    private fun billingExecuteRequestUri(billingKey: String) = "${baseUrl}/v1/billing/${billingKey}"
    private fun paymentCancelRequestUri(paymentKey: String) = "${baseUrl}/v1/payments/${paymentKey}/cancel"

    fun issueBillingKey(
        authKey: String,
        customerKey: String
    ): TosspaymentsBillingKeyIssueResponse? {
        val request = TosspaymentsBillingKeyIssueRequest(
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
            .bodyToMono(TosspaymentsBillingKeyIssueResponse::class.java)
            .doOnError { throw BillingKeyIssuanceException() }
            .block()
    }

    fun executeBilling(
        billingKey: String,
        customerKey: String,
        amount: Long,
        orderId: String,
        orderName: String
    ): TosspaymentsResponse? {
        val request = TosspaymentsBillingExecuteRequest(
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
            .bodyToMono(TosspaymentsResponse::class.java)
            .doOnError { throw BillingExecuteException() }
            .block()
    }

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): TosspaymentsResponse? {
        val request = TosspaymentsPaymentCancelRequest(
            cancelReason = cancelReason
        )

        return WebClient.create()
            .post()
            .uri(paymentCancelRequestUri(paymentKey))
            .headers { header -> header.setBasicAuth(secretKey) }
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToMono(TosspaymentsResponse::class.java)
            .doOnError { throw PaymentCancelException() }
            .block()
    }
}