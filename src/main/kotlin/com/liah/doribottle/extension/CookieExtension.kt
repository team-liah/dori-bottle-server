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
    .domain(".${parseSubDomain(url)}")
    .sameSite("None")
    .httpOnly(true)
    .secure(true)
    .path("/")
    .maxAge(expiredMs/1000)
    .build()

private fun parseSubDomain(url: String): String {
    var part = URI(url).host.split(".")
    return if(part.size > 1) {
        if (part.first() == "www") {
            part = part.drop(1)
        }
        part.joinToString(".")
    } else {
        part.first()
    }
}