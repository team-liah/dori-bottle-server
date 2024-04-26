package com.liah.doribottle.service.sms

import com.liah.doribottle.config.properties.AppProperties
import com.liah.doribottle.service.sms.dto.NcloudSmsSendRequest
import com.liah.doribottle.service.sms.dto.NcloudSmsSendResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant

@Component
class NcloudApiClient(
    appProperties: AppProperties,
) : SmsApiClient {
    companion object {
        const val TIMESTAMP_HEADER = "x-ncp-apigw-timestamp"
        const val SECRET_KEY_HEADER = "x-ncp-iam-access-key"
        const val ACCESS_KEY_HEADER = "x-ncp-apigw-signature-v2"
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val sms = appProperties.ncloud.notification.sms

    override fun sendSms(
        from: String,
        to: String,
        content: String,
    ) {
        val request = NcloudSmsSendRequest(from, to, content)
        val url = "${sms.baseUrl}/${sms.serviceId}/messages"

        val flux =
            WebClient.create()
                .post()
                .uri(url)
                .header(
                    TIMESTAMP_HEADER,
                    Instant.now().toEpochMilli().toString(),
                    ACCESS_KEY_HEADER,
                    "",
                    SECRET_KEY_HEADER,
                    "",
                )
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(NcloudSmsSendResponse::class.java)

        flux.subscribe { response ->
            if (response.statusCode != "202") {
                log.error("ErrorRecipientNo : $to")
                log.error("ErrorRequestId : ${response.requestId}")
                log.error("ErrorCode : ${response.statusCode}")
            }
        }
    }
}
