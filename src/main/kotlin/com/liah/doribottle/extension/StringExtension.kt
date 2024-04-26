package com.liah.doribottle.extension

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Month
import java.time.Year
import java.util.*
import java.util.concurrent.ThreadLocalRandom

fun Any.convertAnyToString(): String {
    return jacksonObjectMapper().writeValueAsString(this)
}

fun <T> String.convertStringToAny(): T {
    val typeReference = object : TypeReference<T>() {}
    return jacksonObjectMapper().readValue(this, typeReference)
}

fun generateRandomString(size: Int): String {
    val randomString = Base64.getUrlEncoder().encodeToString(UUID.randomUUID().toString().toByteArray())
    return randomString.substring(0 until size)
}

fun generateRandomNumberString() = String.format("%06d", ThreadLocalRandom.current().nextInt(999999))

fun getStartDateString(
    year: Year,
    month: Month?,
): String {
    return month?.let { "${year.value}${String.format("%02d", it.value)}01" } ?: "${year.value}0101"
}

fun getEndDateString(
    year: Year,
    month: Month?,
): String {
    return if (month == null || month == Month.DECEMBER) {
        "${year.plusYears(1).value}0101"
    } else {
        "${year.value}${String.format("%02d", month.plus(1).value)}01"
    }
}
