package com.liah.doribottle.config

import com.liah.doribottle.config.security.AuditorAwareImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import java.util.*

@Configuration
@EnableJpaAuditing
class JpaConfig {
    @Bean
    fun auditorProvider(): AuditorAware<UUID> {
        return AuditorAwareImpl()
    }
}