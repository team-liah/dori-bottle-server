package com.liah.doribottle.apiclient

interface SmsApiClient {
    fun sendSms(
        to: String,
        content: String,
    )
}
