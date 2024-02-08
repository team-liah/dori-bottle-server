package com.liah.doribottle.extension

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.util.*
import java.util.concurrent.ThreadLocalRandom

fun Any.convertAnyToString(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}

fun <T> String.convertStringToAny(): T {
    val typeReference = object : TypeReference<T>() {}
    return  jacksonObjectMapper().readValue(this, typeReference)
}

fun generateRandomString(size: Int): String {
    val randomString = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().toByteArray())
    return randomString.substring(0 until size)
}

fun generateRandomNumberString() = String.format("%06d", ThreadLocalRandom.current().nextInt(999999))