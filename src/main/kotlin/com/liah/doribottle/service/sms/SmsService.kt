package com.liah.doribottle.service.sms

import com.liah.doribottle.service.sms.dto.ToastTemplate.LOGIN
import org.springframework.stereotype.Service

@Service
class SmsService(
    private val toastApiClient: ToastApiClient
) {
    fun sendLoginAuthSms(
        phoneNumber: String,
        authCode: String
    ) {
        toastApiClient
            .sendAuthSmsTemplate(LOGIN, phoneNumber, mapOf("authCode" to authCode))
    }
}