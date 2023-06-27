package com.liah.doribottle.service.sms.dto

enum class ToastTemplate(
    val key: String,
    val title: String
) {
    LOGIN("LOGIN_AUTH", "로그인 인증")
}