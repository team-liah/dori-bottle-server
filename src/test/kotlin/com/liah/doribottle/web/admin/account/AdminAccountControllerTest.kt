package com.liah.doribottle.web.admin.account

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.account.vm.AuthRequest
import org.hamcrest.Matchers
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminAccountControllerTest : BaseControllerTest() {
    private val endPoint = "/admin/api/account"

    @Autowired
    private lateinit var adminRepository: AdminRepository

    private lateinit var admin: Admin

    @BeforeEach
    internal fun init() {
        admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, encodePassword("123456"), "Admin", Role.ADMIN))
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