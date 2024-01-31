package com.liah.doribottle.service.sms.dto

import java.time.Instant

data class NcloudSmsSendResponse(
    val requestId: String,
    val requestTime: Instant,
    val statusCode: String,
    val statusName: String,
)