package com.liah.doribottle.service.payment

import com.liah.doribottle.service.payment.dto.BillingKeyIssueRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Service
class TossPaymentsApiClient(
    @Value("\${app.toss.payments.url}") private val url: String,
    @Value("\${app.toss.payments.secretKey}") private val secretKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val billingKeyIssueRequestUrl = "${url}/v1/billing/authorizations/issue"

    fun issueBillingKey(
        authKey: String,
        customerKey: String
    ) {
        val request = BillingKeyIssueRequest(
            authKey = authKey,
            customerKey = customerKey
        ).toHttpEntityForJson(secretKey)

        val response = try {
            RestTemplate().postForEntity<Any>(
                url = billingKeyIssueRequestUrl,
                request = request
            )
        } catch (e: HttpClientErrorException) {
            null
        }
    }
}