package com.liah.doribottle.service.payment.client.kakaopay

import com.liah.doribottle.common.error.exception.BillingKeyIssuanceException
import com.liah.doribottle.service.payment.client.kakaopay.vm.KakaopayPaymentReadyRequest
import com.liah.doribottle.service.payment.client.kakaopay.vm.KakaopayPaymentReadyResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity
import java.util.*

@Component
class KakaopayApiClient(
    @Value("\${app.payments.kakaopay.baseUrl}") private val baseUrl: String,
    @Value("\${app.payments.kakaopay.adminKey}") private val adminKey: String,
    @Value("\${app.payments.kakaopay.cid}") private val cid: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val paymentReadyRequestUrl = "${baseUrl}/v1/payment/ready"

    fun readyPayment(
        userId: UUID
    ): KakaopayPaymentReadyResponse? {
        val request = KakaopayPaymentReadyRequest(
            cid = cid,
            partnerUserId = userId.toString(),
            approvalUrl = "",
            cancelUrl = "",
            failUrl = ""
        ).toHttpEntityForJson(adminKey)

        return try {
            RestTemplate().postForEntity<KakaopayPaymentReadyResponse>(
                url = paymentReadyRequestUrl,
                request = request
            ).body
        } catch (e: Exception) {
            log.error(e.message)
            throw BillingKeyIssuanceException()
        }
    }
}