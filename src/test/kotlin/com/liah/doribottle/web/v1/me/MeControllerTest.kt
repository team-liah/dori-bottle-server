package com.liah.doribottle.web.v1.me

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.user.RefreshTokenRepository
import com.liah.doribottle.repository.user.UserRepository
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MeControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/api/v1/me"

    companion object {
        private const val USER_LOGIN_ID = "010-5638-3316"
    }

    @Autowired private lateinit var context: WebApplicationContext

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired private lateinit var tokenProvider: TokenProvider

    private lateinit var user: User
    private lateinit var userRefreshToken: RefreshToken

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
        userRefreshToken = refreshTokenRepository.save(RefreshToken(user))
    }

    @AfterEach
    internal fun destroy() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("Dori User 프로필 조회")
    @WithMockDoriUser(loginId = "010-5638-3316", role = Role.USER)
    @Test
    fun get() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("loginId", `is`("010-5638-3316")))
            .andExpect(jsonPath("role", `is`(Role.USER.name)))
    }

    @DisplayName("프로필 조회")
    @Test
    fun getProfile() {
        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val cookie = Cookie(ACCESS_TOKEN, accessToken)

        mockMvc.perform(
            MockMvcRequestBuilders.get("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("birthDate", `is`(user.birthDate)))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
    }
}