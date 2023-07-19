package com.liah.doribottle.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@EnableAsync
@Configuration
class AsyncConfig {
    @Bean
    fun threadPoolTaskExecutor(): ThreadPoolTaskExecutor? {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 15
        executor.maxPoolSize = 25
        executor.queueCapacity = 100
        executor.setThreadNamePrefix("async-")
        return executor
    }
}