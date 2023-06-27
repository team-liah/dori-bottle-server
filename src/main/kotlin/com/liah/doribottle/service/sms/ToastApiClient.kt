package com.liah.doribottle.service.sms

import com.liah.doribottle.service.sms.dto.Recipient
import com.liah.doribottle.service.sms.dto.SendAuthSmsTemplateRequest
import com.liah.doribottle.service.sms.dto.ToastRestApiResponse
import com.liah.doribottle.service.sms.dto.ToastTemplate
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class ToastApiClient(
    @Value("\${toast.sms.url}") private val smsUrl: String,
    @Value("\${toast.sms.appKey}") private val smsAppKey: String,
    @Value("\${toast.sms.secretKey}") private val smsSecretKey: String,
    @Value("\${toast.sms.sendNo}") private val sendNo: String
) {
    private val authSmsSendRequestUrl = "${smsUrl}/sms/v3.0/appKeys/${smsAppKey}/sender/auth/sms"

    fun sendAuthSmsTemplate(
        template: ToastTemplate,
        recipientNo: String,
        templateParameter: Map<String, String>
    ) {
        val recipient = Recipient(recipientNo, "82", templateParameter)
        val headers = HttpHeaders()
        headers["X-Secret-Key"] = smsSecretKey
        val request = SendAuthSmsTemplateRequest(template.key, sendNo, listOf(recipient)).toHttpEntityForJson(headers)

        val response = RestTemplate()
            .postForEntity(authSmsSendRequestUrl, request, ToastRestApiResponse::class.java)

        if (response.statusCode != HttpStatus.OK || response.body?.header?.isSuccessful == false)
            throw Exception("SMS 발송 실패했습니다.")
    }
}