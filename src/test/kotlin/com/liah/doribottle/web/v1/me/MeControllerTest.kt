package com.liah.doribottle.web.v1.me

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.user.RefreshTokenRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MeControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/me"

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var user: User
    private lateinit var userRefreshToken: RefreshToken

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
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
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
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)

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