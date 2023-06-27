package com.liah.doribottle.service.sms.dto

data class ToastRestApiResponse(
    val header: Header
)

data class Header(
    val resultCode: Int,
    val resultMessage: String,
    val isSuccessful: Boolean
)