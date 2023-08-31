package com.liah.doribottle.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@ConditionalOnProperty(
    value = ["app.schedule.enabled"],
    matchIfMissing = true,
    havingValue = "true"
)
@EnableScheduling
@Configuration
class ScheduleConfig