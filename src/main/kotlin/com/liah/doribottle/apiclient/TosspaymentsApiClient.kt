package com.liah.doribottle.apiclient

import com.liah.doribottle.apiclient.vm.TosspaymentsBillingExecuteRequest
import com.liah.doribottle.apiclient.vm.TosspaymentsBillingKeyIssueRequest
import com.liah.doribottle.apiclient.vm.TosspaymentsBillingKeyIssueResponse
import com.liah.doribottle.apiclient.vm.TosspaymentsPaymentCancelRequest
import com.liah.doribottle.apiclient.vm.TosspaymentsResponse
import com.liah.doribottle.config.properties.AppProperties
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient

@Service
class TosspaymentsApiClient(
    appProperties: AppProperties,
    webClient: WebClient,
) : BaseApiClient(webClient) {
    companion object {
        const val BASIC_AUTH_HEADER = "Authorization"
    }

    private val baseUrl = appProperties.tosspayments.baseUrl
    private val secretKey = appProperties.tosspayments.secretKey
    private val billingKeyIssueRequestUri = "$baseUrl/v1/billing/authorizations/issue"

    private fun billingExecuteRequestUri(billingKey: String) = "$baseUrl/v1/billing/$billingKey"

    private fun paymentCancelRequestUri(paymentKey: String) = "$baseUrl/v1/payments/$paymentKey/cancel"

    fun issueBillingKey(
        authKey: String,
        customerKey: String,
    ): TosspaymentsBillingKeyIssueResponse? {
        val request =
            TosspaymentsBillingKeyIssueRequest(
                authKey = authKey,
                customerKey = customerKey,
            )

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(BASIC_AUTH_HEADER, "Basic $secretKey")

        return retrievePostForMono(
            uri = billingKeyIssueRequestUri,
            headers = headers,
            requestBody = request,
            responseType = TosspaymentsBillingKeyIssueResponse::class.java,
        ).block()
    }

    fun executeBilling(
        billingKey: String,
        customerKey: String,
        amount: Long,
        orderId: String,
        orderName: String,
    ): TosspaymentsResponse? {
        val request =
            TosspaymentsBillingExecuteRequest(
                customerKey = customerKey,
                amount = amount,
                orderId = orderId,
                orderName = orderName,
            )

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(BASIC_AUTH_HEADER, "Basic $secretKey")

        return retrievePostForMono(
            uri = billingExecuteRequestUri(billingKey),
            headers = headers,
            requestBody = request,
            responseType = TosspaymentsResponse::class.java,
        ).block()
    }

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String,
    ): TosspaymentsResponse? {
        val request =
            TosspaymentsPaymentCancelRequest(
                cancelReason = cancelReason,
            )

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(BASIC_AUTH_HEADER, "Basic $secretKey")

        return retrievePostForMono(
            uri = paymentCancelRequestUri(paymentKey),
            headers = headers,
            requestBody = request,
            responseType = TosspaymentsResponse::class.java,
        ).block()
    }
}
