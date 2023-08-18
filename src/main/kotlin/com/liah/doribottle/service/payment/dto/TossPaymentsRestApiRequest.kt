package com.liah.doribottle.service.payment.dto

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType

open class TossPaymentsRestApiRequest {
    fun toHttpEntityForJson(secretKey: String): HttpEntity<TossPaymentsRestApiRequest> {
        val headers = HttpHeaders()
        headers.setBasicAuth(secretKey)
        headers.contentType = MediaType.APPLICATION_JSON
        return HttpEntity(this, headers)
    }
}