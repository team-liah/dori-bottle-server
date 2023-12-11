package com.liah.doribottle.config

import org.springframework.boot.test.context.TestConfiguration
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class TestcontainersConfig {
    companion object {
        private const val MARIADB_IMAGE_NAME = "mariadb:10.6"
        private const val REDIS_IMAGE_NAME = "redis:latest"
        private val MARIADB_CONTAINER = MariaDBContainer(MARIADB_IMAGE_NAME).withCommand("mysqld", "--character-set-server=utf8mb4")
        private val REDIS_CONTAINER: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse(REDIS_IMAGE_NAME))
            .withExposedPorts(6379)

        init {
            MARIADB_CONTAINER.start()
            System.setProperty("DB_URL", MARIADB_CONTAINER.getJdbcUrl())
            System.setProperty("DB_USERNAME", MARIADB_CONTAINER.username)
            System.setProperty("DB_PASSWORD", MARIADB_CONTAINER.password)

            REDIS_CONTAINER.start()
            System.setProperty("REDIS_HOST", REDIS_CONTAINER.host)
            System.setProperty("REDIS_PORT", REDIS_CONTAINER.getMappedPort(6379).toString())
        }
    }
}