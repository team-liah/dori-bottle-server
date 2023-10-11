package com.liah.doribottle.service.sms

import com.liah.doribottle.service.sms.dto.ToastSmsSendApiResponse
import com.liah.doribottle.service.sms.dto.ToastSmsSendRequest
import com.liah.doribottle.service.sms.dto.ToastTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ToastApiClient(
    @Value("\${app.toast.sms.baseUrl}") private val smsBaseUrl: String,
    @Value("\${app.toast.sms.appKey}") private val smsAppKey: String,
    @Value("\${app.toast.sms.secretKey}") private val smsSecretKey: String,
    @Value("\${app.toast.sms.sendNo}") private val sendNo: String
) {
    private val log = LoggerFactory.getLogger(javaClass)
    private val authSmsSendRequestUri = "${smsBaseUrl}/sms/v3.0/appKeys/${smsAppKey}/sender/auth/sms"

    fun sendSms(
        template: ToastTemplate,
        recipientNo: String,
        templateParameter: Map<String, String>
    ) {
        val request = ToastSmsSendRequest(
            templateId = template.id,
            sendNo = sendNo,
            templateParameter = templateParameter,
            recipientNo
        )

        val flux = WebClient.create()
            .post()
            .uri(authSmsSendRequestUri)
            .header("X-Secret-Key", smsSecretKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(ToastSmsSendApiResponse::class.java)

        flux.subscribe { response ->
             if (response.header?.isSuccessful == false) {
                 log.error("ErrorRecipientNo : $recipientNo")
                 log.error("ErrorCode : ${response.header.resultCode}")
                 log.error("ErrorMessage : ${response.header.resultMessage}")
             }
        }
    }
}