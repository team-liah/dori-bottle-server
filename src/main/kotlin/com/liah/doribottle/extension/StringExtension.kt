package com.liah.doribottle.extension

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.concurrent.ThreadLocalRandom

fun Any.convertAnyToString(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}

fun <T> String.convertStringToAny(): T {
    val typeReference = object : TypeReference<T>() {}
    return  jacksonObjectMapper().readValue(this, typeReference)
}

private val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun generateRandomString(size: Int) = List(size) { charPool.random() }.joinToString("")

fun generateRandomNumberString() = String.format("%06d", ThreadLocalRandom.current().nextInt(999999))