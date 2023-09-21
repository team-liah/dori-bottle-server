package com.liah.doribottle.config

import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.user.LoginIdChangeRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisKeyValueAdapter
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories

@EnableRedisRepositories(
    enableKeyspaceEvents = RedisKeyValueAdapter.EnableKeyspaceEvents.ON_STARTUP,
    basePackageClasses = [
        RefreshTokenRepository::class,
        AlertRepository::class,
        LoginIdChangeRepository::class
    ]
)
@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val host: String,
    @Value("\${spring.data.redis.port}") private val port: Int
) {
    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(RedisStandaloneConfiguration(host, port))
}