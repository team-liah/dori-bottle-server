package com.liah.doribottle.common.exhandler

data class FieldErrorResponse (
    val field: String?,
    val message: String?,
    val status: Int
)