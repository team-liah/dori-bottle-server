package com.liah.doribottle.apiclient

import com.liah.doribottle.apiclient.vm.NcloudSmsSendRequest
import com.liah.doribottle.apiclient.vm.NcloudSmsSendResponse
import com.liah.doribottle.config.properties.AppProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import java.time.Instant
import java.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

@Component
class NcloudApiClient(
    appProperties: AppProperties,
    webClient: WebClient,
) : BaseApiClient(webClient), SmsApiClient {
    companion object {
        const val TIMESTAMP_HEADER = "x-ncp-apigw-timestamp"
        const val ACCESS_KEY_HEADER = "x-ncp-iam-access-key"
        const val SIGNATURE_HEADER = "x-ncp-apigw-signature-v2"

        private const val SPACE = " "
        private const val NEW_LINE = "\n"
        private const val ALGORITHM = "HmacSHA256"

        fun makeSignature(
            method: String,
            url: String,
            timestamp: String,
            accessKey: String,
            secretKey: String,
        ): String {
            val message =
                StringBuilder()
                    .append(method)
                    .append(SPACE)
                    .append(url)
                    .append(NEW_LINE)
                    .append(timestamp)
                    .append(NEW_LINE)
                    .append(accessKey)
                    .toString()

            val signingKey = SecretKeySpec(secretKey.toByteArray(Charsets.UTF_8), ALGORITHM)
            val mac = Mac.getInstance(ALGORITHM)
            mac.init(signingKey)

            val rawHmac = mac.doFinal(message.toByteArray(Charsets.UTF_8))

            return Base64.getEncoder().encodeToString(rawHmac)
        }
    }

    private val log = LoggerFactory.getLogger(javaClass)
    private val accessKey = appProperties.ncloud.accessKey
    private val secretKey = appProperties.ncloud.secretKey
    private val sms = appProperties.ncloud.notification.sms
    private val smsPath = "${sms.servicePath}/${sms.serviceId}/messages"
    private val smsUrl = "${appProperties.ncloud.baseUrl}$smsPath"

    override fun sendSms(
        to: String,
        content: String,
    ) {
        val request = NcloudSmsSendRequest(sms.callingNumber, to, content)
        val timestamp = Instant.now().toEpochMilli().toString()
        val signature = makeSignature("POST", smsPath, timestamp, accessKey, secretKey)

        val headers = LinkedMultiValueMap<String, String>()
        headers.add(TIMESTAMP_HEADER, timestamp)
        headers.add(ACCESS_KEY_HEADER, accessKey)
        headers.add(SIGNATURE_HEADER, signature)

        retrievePostForMono(
            uri = smsUrl,
            headers = headers,
            requestBody = request,
            responseType = NcloudSmsSendResponse::class.java,
        ).subscribe { response ->
            log.error("ErrorRecipientNo : $to")
            log.error("ErrorRequestId : ${response.requestId}")
            log.error("ErrorCode : ${response.statusCode}")
        }
    }
}
