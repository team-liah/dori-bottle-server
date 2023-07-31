package com.liah.doribottle.extension

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

fun Any?.convertJsonToString(): String {
    return jacksonObjectMapper().writeValueAsString(this) ?: ""
}

private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun randomString(size: Int) = List(size) { charPool.random() }.joinToString("")