package com.liah.doribottle.domain.user

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash(value = "loginIdChangeRequest")
data class LoginIdChangeRequest(
    @Id
    var userId: String?,

    @TimeToLive
    val ttl: Long = 300,

    val toLoginId: String?,

    val authCode: String?
)