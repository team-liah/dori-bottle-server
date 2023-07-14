package com.liah.doribottle.web.account

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.user.RefreshTokenRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.account.vm.AuthRequest
import com.liah.doribottle.web.account.vm.RegisterRequest
import com.liah.doribottle.web.account.vm.SendSmsRequest
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AccountControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/api/v1/account"

    companion object {
        private const val USER_LOGIN_ID = "010-5638-3316"
        private const val GUEST_LOGIN_ID = "010-1234-5678"
    }
    @Autowired private lateinit var context: WebApplicationContext

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var user: User
    private lateinit var guest: User

    private lateinit var userRefreshToken: RefreshToken
    private lateinit var guestRefreshToken: RefreshToken

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(springSecurity())
            .build()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.updatePassword(passwordEncoder.encode("123456"))
        user = userRepository.save(userEntity)
        userRefreshToken = refreshTokenRepository.save(RefreshToken(user))

        guest = userRepository.save(User(GUEST_LOGIN_ID, "사용자", GUEST_LOGIN_ID, Role.GUEST))
        guestRefreshToken = refreshTokenRepository.save(RefreshToken(guest))
    }

    @AfterEach
    internal fun destroy() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("인증요청")
    @Test
    fun sendSms() {
        val body = SendSmsRequest(USER_LOGIN_ID)

        mockMvc.perform(
            post("$endPoint/auth/send-sms")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(MockMvcResultMatchers.status().is5xxServerError)
            .andExpect(jsonPath("message", `is`(ErrorCode.SMS_SENDING_ERROR.message)))
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        val body = AuthRequest(USER_LOGIN_ID, "123456")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(MockMvcResultMatchers.cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 - Unauthorized")
    @Test
    fun authException() {
        val body = AuthRequest(USER_LOGIN_ID, "000000")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
    }

    @DisplayName("인증 새로고침")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun refreshAuth() {
        val cookie = Cookie(REFRESH_TOKEN, userRefreshToken.token)

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(MockMvcResultMatchers.cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 새로고침 - Unauthorized")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun refreshAuthException() {
        val cookie = Cookie(REFRESH_TOKEN, UUID.randomUUID().toString())

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isUnauthorized)
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
    }

    @DisplayName("로그아웃")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun logout() {
        val cookie = Cookie(REFRESH_TOKEN, userRefreshToken.token)

        mockMvc.perform(
            post("$endPoint/logout")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.cookie().value(ACCESS_TOKEN, ""))
            .andExpect(MockMvcResultMatchers.cookie().value(REFRESH_TOKEN, ""))
    }

    @DisplayName("회원가입")
    @WithMockDoriUser(loginId = GUEST_LOGIN_ID, role = Role.GUEST)
    @Test
    fun register() {
        val cookie = Cookie(REFRESH_TOKEN, guestRefreshToken.token)
        val body = RegisterRequest("Tester 2", MALE, "19970101", true, true, false)

        mockMvc
            .perform(
            post("$endPoint/register")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(MockMvcResultMatchers.cookie().value(REFRESH_TOKEN, notNullValue()))
    }
}