package com.liah.doribottle.service.sms.dto

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

open class ToastRestApiRequest {
    fun toHttpEntityForJson(secretKey: String): HttpEntity<ToastRestApiRequest> {
        val headers = HttpHeaders()
        headers["X-Secret-Key"] = secretKey
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(this, headers)
    }
}