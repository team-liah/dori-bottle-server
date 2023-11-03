package com.liah.doribottle.web

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.domain.user.Role
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BaseControllerTest {
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var context: WebApplicationContext
    @Autowired
    private lateinit var tokenProvider: TokenProvider
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    companion object {
        const val ADMIN_LOGIN_ID = "adminTester"
        const val MACHINE_LOGIN_ID = "machine"
        const val USER_LOGIN_ID = "010-5638-3316"
        const val GUEST_LOGIN_ID = "010-1234-5678"
        const val CUP_RFID = "A1:A1:A1:A1"

        private const val REDIS_IMAGE_NAME = "redis:latest"
        private val REDIS_CONTAINER: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse(REDIS_IMAGE_NAME))
            .withExposedPorts(6379)

        init {
            REDIS_CONTAINER.start()
            System.setProperty("spring.data.redis.host", REDIS_CONTAINER.host)
            System.setProperty("spring.data.redis.port", REDIS_CONTAINER.getMappedPort(6379).toString())
        }
    }

    protected fun createAccessTokenCookie(
        id: UUID,
        loginId: String,
        name: String,
        role: Role
    ): Cookie {
        val accessToken = tokenProvider.generateAccessToken(id, loginId, name, role)

        return Cookie(ACCESS_TOKEN, accessToken)
    }

    protected fun encodePassword(rawPassword: String): String = passwordEncoder.encode(rawPassword)
}