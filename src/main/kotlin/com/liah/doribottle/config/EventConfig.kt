package com.liah.doribottle.config

import com.liah.doribottle.event.Events
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class EventConfig(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun eventsInitializer() = InitializingBean { Events.setPublisher(applicationContext) }
}