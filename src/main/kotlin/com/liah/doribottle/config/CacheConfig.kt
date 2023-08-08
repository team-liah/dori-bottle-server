package com.liah.doribottle.config

import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager.RedisCacheManagerBuilder
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@EnableCaching
@Configuration
class CacheConfig {
    @Bean
    fun redisCacheManagerBuilderCustomizer(): RedisCacheManagerBuilderCustomizer {
        return RedisCacheManagerBuilderCustomizer { builder: RedisCacheManagerBuilder ->
            builder
                .withCacheConfiguration(
                    "pointSum",
                    RedisCacheConfiguration.defaultCacheConfig()
                        .computePrefixWith { cacheName -> "cache :: $cacheName" }
                        .entryTtl(Duration.ofDays(1))
                        .disableCachingNullValues()
                        .serializeKeysWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer())
                        )
                        .serializeValuesWith(
                            RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())
                        )
                )
        }
    }
}