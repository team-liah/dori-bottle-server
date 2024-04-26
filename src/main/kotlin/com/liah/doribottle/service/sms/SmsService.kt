package com.liah.doribottle.service.sms

import org.springframework.stereotype.Service

@Service
class SmsService(
    private val ncloudApiClient: NcloudApiClient
) {
    fun sendAuthSms(
        phoneNumber: String,
        authCode: String
    ) {
        ncloudApiClient.sendSms(
            to = phoneNumber.replace("-", ""),
            content = smsAuthCodeTemplate(authCode)
        )
    }

    private fun smsAuthCodeTemplate(authCode: String): String {
        return "[도리보틀] 인증번호 [$authCode] *타인에게 절대 알리지 마세요."
    }
}