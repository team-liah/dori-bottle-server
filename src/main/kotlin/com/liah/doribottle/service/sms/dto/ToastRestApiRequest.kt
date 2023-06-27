package com.liah.doribottle.service.sms.dto

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

open class ToastRestApiRequest {
    fun toHttpEntityForJson(headers: HttpHeaders = HttpHeaders()): HttpEntity<ToastRestApiRequest> {
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(this, headers)
    }
}