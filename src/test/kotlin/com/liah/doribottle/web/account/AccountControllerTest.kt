package com.liah.doribottle.web.account

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.web.account.vm.AuthRequest
import com.liah.doribottle.web.account.vm.RegisterRequest
import com.liah.doribottle.web.account.vm.SendSmsRequest
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.*
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
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
        private const val USER_LOGIN_ID = "01056383316"
        private const val GUEST_LOGIN_ID = "01012345678"
    }
    @Autowired private lateinit var context: WebApplicationContext

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var tokenProvider: TokenProvider
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
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()

        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.updatePassword(passwordEncoder.encode("123456"))
        user = userRepository.save(userEntity)
        userRefreshToken = refreshTokenRepository.save(RefreshToken(user))

        guest = userRepository.save(User(GUEST_LOGIN_ID, "사용자", GUEST_LOGIN_ID, Role.GUEST))
        guestRefreshToken = refreshTokenRepository.save(RefreshToken(guest))
    }

    @DisplayName("Dori User 프로필 조회")
    @Test
    fun getSimpleProfile() {
        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val cookie = Cookie(ACCESS_TOKEN, accessToken)

        mockMvc.perform(
            get("$endPoint/simple-profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
    }

    @DisplayName("프로필 조회")
    @Test
    fun getProfile() {
        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val cookie = Cookie(ACCESS_TOKEN, accessToken)

        mockMvc.perform(
            get("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("birthDate", `is`(user.birthDate)))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
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
            .andExpect(jsonPath("message", `is`("SMS 발송 실패했습니다.")))
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
            .andExpect(jsonPath("message", `is`("잘못된 인증번호입니다.")))
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
            .andExpect(jsonPath("message", `is`("유효한 토큰 정보를 확인할 수 없습니다.")))
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
        val body = RegisterRequest(GUEST_LOGIN_ID, "Tester 2", Gender.MALE, 19970101, true, true, false)

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