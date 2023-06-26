package com.liah.doribottle.extension

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Any?.convertJsonToString(): String {
    return jacksonObjectMapper().writeValueAsString(this) ?: ""
}