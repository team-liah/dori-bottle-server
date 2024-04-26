package com.liah.doribottle.service.sms

interface SmsApiClient {
    fun sendSms(
        to: String,
        content: String,
    )
}
