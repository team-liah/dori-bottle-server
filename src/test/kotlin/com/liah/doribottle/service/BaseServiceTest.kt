package com.liah.doribottle.service

import com.liah.doribottle.config.RedisTestContainerConfig
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@Import(RedisTestContainerConfig::class)
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
    }
}