package com.liah.doribottle

import com.liah.doribottle.config.properties.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@EnableConfigurationProperties(AppProperties::class)
@SpringBootApplication
class DoriBottleApplication

fun main(args: Array<String>) {
	runApplication<DoriBottleApplication>(*args)
}
