package com.liah.doribottle.config.security

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive
import java.util.*

@RedisHash(value = "refreshToken")
data class RefreshToken(
    @Id
    var refreshToken: String? = UUID.randomUUID().toString(),

    @TimeToLive
    val ttl: Long = 1209600,

    val userId: String?
)