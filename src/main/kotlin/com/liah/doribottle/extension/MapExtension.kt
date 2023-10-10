package com.liah.doribottle.extension

import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun Any.convertAnyToMultiValueMap(): MultiValueMap<String, String> {
    val params = LinkedMultiValueMap<String, String>()
    val map = jacksonObjectMapper().convertValue<Map<String, String>>(this)
    params.setAll(map)

    return params
}