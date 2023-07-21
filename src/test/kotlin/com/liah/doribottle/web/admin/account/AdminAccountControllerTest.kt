package com.liah.doribottle.web.admin.account

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.admin.account.vm.AuthRequest
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AdminAccountControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/admin/api/account"

    companion object {
        private const val ADMIN_LOGIN_ID = "tester"
    }
    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var adminRepository: AdminRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private lateinit var admin: Admin

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @BeforeEach
    internal fun init() {
        val encodedPassword = passwordEncoder.encode("123456")
        admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, encodedPassword, "Admin", Role.ADMIN))
    }

    @AfterEach
    internal fun destroy() {
        adminRepository.deleteAll()
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        val body = AuthRequest(ADMIN_LOGIN_ID, "123456")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, Matchers.notNullValue()))
    }

    @DisplayName("로그아웃")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun logout() {
        mockMvc.perform(
            post("$endPoint/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, ""))
    }
}