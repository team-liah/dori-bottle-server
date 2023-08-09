package com.liah.doribottle.domain.notification

import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash(value = "alert")
data class Alert(
    @Id
    var userId: String?,

    var count: Int = 0
) {
    fun increaseCount() {
        count += 1
    }
}