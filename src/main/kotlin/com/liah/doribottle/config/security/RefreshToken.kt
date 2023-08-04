package com.liah.doribottle.config.security

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import java.util.*

@RedisHash(value = "refreshToken", timeToLive = 60)
class RefreshToken(
    @Id
    val refreshToken: UUID,
    val userId: UUID
)