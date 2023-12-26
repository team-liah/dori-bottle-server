package com.liah.doribottle.service.sms

import com.liah.doribottle.service.sms.dto.ToastTemplate
import org.springframework.stereotype.Service

@Service
class SmsService(
//    private val toastApiClient: ToastApiClient
) {
    fun sendAuthSms(
        phoneNumber: String,
        authCode: String
    ) {
//        toastApiClient.sendSms(
//            template = ToastTemplate.AUTH,
//            recipientNo = phoneNumber.replace("-", ""),
//            templateParameter = mapOf("authCode" to authCode)
//        )
    }
}