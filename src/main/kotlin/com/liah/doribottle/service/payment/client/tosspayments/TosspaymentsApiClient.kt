package com.liah.doribottle.service.payment.client.tosspayments

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.service.payment.dto.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Component
class TosspaymentsApiClient(
    @Value("\${app.payments.tosspayments.baseUrl}") private val baseUrl: String,
    @Value("\${app.payments.tosspayments.secretKey}") private val secretKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val billingKeyIssueRequestUrl = "${baseUrl}/v1/billing/authorizations/issue"
    private fun billingExecuteRequestUrl(billingKey: String) = "${baseUrl}/v1/billing/${billingKey}"
    private fun paymentCancelRequestUrl(paymentKey: String) = "${baseUrl}/v1/payments/${paymentKey}/cancel"

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
        ).toHttpEntityForJson(secretKey)

        return try {
            RestTemplate().postForEntity<TossPaymentResponse>(
                url = billingExecuteRequestUrl(billingKey),
                request = request
            ).body
        } catch (e: Exception) {
            log.error(e.message)
            throw BillingExecuteException()
        }
    }

    fun cancelPayment(
        paymentKey: String,
        cancelReason: String
    ): TossPaymentResponse? {
        val request = TossPaymentCancelRequest(
            cancelReason = cancelReason
        ).toHttpEntityForJson(secretKey)

        return try {
            RestTemplate().postForEntity<TossPaymentResponse>(
                url = paymentCancelRequestUrl(paymentKey),
                request = request
            ).body
        } catch (e: Exception) {
            log.error(e.message)
            throw PaymentCancelException()
        }
    }
}