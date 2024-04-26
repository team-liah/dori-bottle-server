package com.liah.doribottle.service.sms.dto

import java.time.LocalDateTime

data class NcloudSmsSendResponse(
    val requestId: String,
    val requestTime: LocalDateTime,
    val statusCode: String,
    val statusName: String,
)