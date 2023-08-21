package com.liah.doribottle.service.sms.dto

data class AuthSmsSendRequest(
    val templateId: String,
    val sendNo: String,
    val recipientList: List<Recipient>
) : ToastRestApiRequest()

data class Recipient(
    val recipientNo: String,
    val countryCode: String,
    val templateParameter: Map<String, String>
)