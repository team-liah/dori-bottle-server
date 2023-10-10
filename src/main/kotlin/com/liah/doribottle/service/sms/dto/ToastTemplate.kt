package com.liah.doribottle.service.sms.dto

enum class ToastTemplate(
    val id: String,
    val title: String
) {
    AUTH("AUTH", "인증코드 발송")
}