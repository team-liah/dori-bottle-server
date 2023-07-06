package com.liah.doribottle.web.me

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MeControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/api/v1/me"

    companion object {
        private const val USER_LOGIN_ID = "01056383316"
    }

    @Autowired private lateinit var context: WebApplicationContext

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var tokenProvider: TokenProvider

    private lateinit var user: User

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @BeforeEach
    internal fun init() {
        userRepository.deleteAll()

        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
    }

    @DisplayName("Dori User 프로필 조회")
    @Test
    fun get() {
        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val cookie = Cookie(ACCESS_TOKEN, accessToken)

        mockMvc.perform(
            MockMvcRequestBuilders.get("$endPoint")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("id", Matchers.`is`(user.id.toString())))
            .andExpect(MockMvcResultMatchers.jsonPath("loginId", Matchers.`is`(user.loginId)))
            .andExpect(MockMvcResultMatchers.jsonPath("role", Matchers.`is`(user.role.name)))
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
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andExpect(MockMvcResultMatchers.jsonPath("id", Matchers.`is`(user.id.toString())))
            .andExpect(MockMvcResultMatchers.jsonPath("loginId", Matchers.`is`(user.loginId)))
            .andExpect(MockMvcResultMatchers.jsonPath("name", Matchers.`is`(user.name)))
            .andExpect(MockMvcResultMatchers.jsonPath("phoneNumber", Matchers.`is`(user.phoneNumber)))
            .andExpect(MockMvcResultMatchers.jsonPath("invitationCode", Matchers.`is`(user.invitationCode)))
            .andExpect(MockMvcResultMatchers.jsonPath("birthDate", Matchers.`is`(user.birthDate)))
            .andExpect(MockMvcResultMatchers.jsonPath("gender", Matchers.`is`(user.gender)))
            .andExpect(MockMvcResultMatchers.jsonPath("role", Matchers.`is`(user.role.name)))
    }
}