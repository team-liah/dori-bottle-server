package com.liah.doribottle.service.payment.client.kakaopay.vm

import com.liah.doribottle.extension.convertAnyToMultiValueMap
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap

open class KakaopayRestApiRequest {
    fun toHttpEntityForJson(adminKey: String): HttpEntity<MultiValueMap<String, String>> {
        val headers = HttpHeaders()
        headers.set("Authorization", "KakaoAK $adminKey")
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED
        return HttpEntity(this.convertAnyToMultiValueMap(), headers)
    }
}