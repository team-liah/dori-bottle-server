package com.liah.doribottle.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.redis.spring.RedisLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.scheduling.annotation.EnableScheduling

@ConditionalOnProperty(
    value = ["app.schedule.enabled"],
    matchIfMissing = true,
    havingValue = "true"
)
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT60S")
@Configuration
class ScheduleConfig {
    @Bean
    fun lockProvider(redisConnectionFactory: RedisConnectionFactory): LockProvider {
        return RedisLockProvider(redisConnectionFactory)
    }
}