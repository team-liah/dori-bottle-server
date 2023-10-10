package com.liah.doribottle.service.sms.dto

data class ToastSmsSendApiResponse(
    val header: Header,
    val body: Any
) {
    data class Header(
        val resultCode: Int,
        val resultMessage: String,
        val isSuccessful: Boolean
    )
}