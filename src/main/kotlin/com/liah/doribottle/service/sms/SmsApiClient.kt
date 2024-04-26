package com.liah.doribottle.service.sms

interface SmsApiClient {
    fun sendSms(
        from: String,
        to: String,
        content: String,
    )
}
