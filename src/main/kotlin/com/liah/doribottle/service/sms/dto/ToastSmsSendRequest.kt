package com.liah.doribottle.service.sms.dto

class ToastSmsSendRequest(
    val templateId: String,
    val sendNo: String,
    templateParameter: Map<String, String>,
    vararg recipientNo: String
) {
    val recipientList: List<Recipient> = recipientNo.map { Recipient(it, "82", templateParameter) }

    data class Recipient(
        val recipientNo: String,
        val countryCode: String,
        val templateParameter: Map<String, String>
    )
}