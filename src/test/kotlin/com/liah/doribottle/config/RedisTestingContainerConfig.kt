package com.liah.doribottle.config

import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@ActiveProfiles("test")
@Configuration
class RedisTestingContainerConfig {
    companion object {
        private const val REDIS_IMAGE_NAME = "redis:5.0.3-alpine"
        private val REDIS_CONTAINER: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse(REDIS_IMAGE_NAME))
            .withExposedPorts(6379)

        init {
            REDIS_CONTAINER.start()
            System.setProperty("spring.data.redis.host", REDIS_CONTAINER.host)
            System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString())
        }
    }
}