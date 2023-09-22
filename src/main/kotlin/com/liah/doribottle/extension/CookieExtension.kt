package com.liah.doribottle.extension

import org.springframework.http.ResponseCookie
import java.net.URI

fun expireCookie(
    url: String,
    name: String
) = createCookie(url, name, "", 0)

fun createCookie(
    url: String,
    name: String,
    value: String,
    expiredMs: Long
) = ResponseCookie.from(name, value)
    .domain(getHost(url))
    .sameSite("None")
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(expiredMs/1000)
    .build()

private fun getHost(url: String) = URI(url).host