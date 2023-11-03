package com.liah.doribottle.service

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@SpringBootTest
@Transactional
class BaseServiceTest {
    @PersistenceContext
    private lateinit var entityManager: EntityManager

    protected fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    companion object {
        const val USER_LOGIN_ID = "010-5638-3316"
        const val ADMIN_LOGIN_ID = "admin"
        const val CUP_RFID = "A1:A1:A1:A1"
        const val MACHINE_NO = "000-00000"
        const val MACHINE_NO1 = "000-00001"
        const val MACHINE_NO2 = "000-00002"
        const val MACHINE_NAME = "XX대학교 정문"
        const val RFID = "RFID"

        private const val REDIS_IMAGE_NAME = "redis:latest"
        private val REDIS_CONTAINER: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse(REDIS_IMAGE_NAME))
            .withExposedPorts(6379)

        init {
            REDIS_CONTAINER.start()
            System.setProperty("spring.data.redis.host", REDIS_CONTAINER.host)
            System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString())
        }
    }
}