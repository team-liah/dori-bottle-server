package com.liah.doribottle.apiclient.vm

import java.time.LocalDateTime

data class NcloudSmsSendResponse(
    val requestId: String,
    val requestTime: LocalDateTime,
    val statusCode: String,
    val statusName: String,
)
