package com.liah.doribottle.common.error.exception

class BadWebClientRequestException(
    val statusCode: Int,
    val statusText: String? = null,
    message: String? = null,
) : RuntimeException(message)
