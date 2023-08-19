package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.service.payment.dto.TossBillingKeyIssueRequest
import com.liah.doribottle.service.payment.dto.TossBillingKeyIssueResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
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
    ): TossBillingKeyIssueResponse? {
        val request = TossBillingKeyIssueRequest(
            authKey = authKey,
            customerKey = customerKey
        ).toHttpEntityForJson(secretKey)

        return try {
            RestTemplate().postForEntity<TossBillingKeyIssueResponse>(
                url = billingKeyIssueRequestUrl,
                request = request
            ).body
        } catch (e: Exception) {
            log.error(e.message)
            throw BillingKeyIssuanceException()
        }
    }
}