package com.liah.doribottle.extension

import org.springframework.http.ResponseCookie

fun expireCookie(
    name: String
) = createCookie(name, "", 0)

fun createCookie(
    name: String,
    value: String,
    expiredMs: Long
) = ResponseCookie.from(name, value)
    .sameSite("None")
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(expiredMs/1000)
    .build()