package com.liah.doribottle.web.admin.account

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.account.vm.AuthRequest
import jakarta.servlet.http.Cookie
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.util.*

class AdminAccountControllerTest : BaseControllerTest() {
    private val endPoint = "/admin/api/account"

    @Autowired
    private lateinit var adminRepository: AdminRepository
    @Autowired
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    private lateinit var admin: Admin
    private lateinit var adminRefreshToken: RefreshToken

    @BeforeEach
    internal fun init() {
        admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, encodePassword("123456"), "Admin", Role.ADMIN, null, null, null))
        adminRefreshToken = refreshTokenRepository.save(RefreshToken(userId = admin.id.toString()))
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
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 - Unauthorized")
    @Test
    fun authException() {
        val body = AuthRequest(ADMIN_LOGIN_ID, "000000")

        mockMvc.perform(
            post("$endPoint/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
    }

    @DisplayName("인증 새로고침")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun refreshAuth() {
        val cookie = Cookie(REFRESH_TOKEN, adminRefreshToken.refreshToken)

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(cookie().value(ACCESS_TOKEN, notNullValue()))
            .andExpect(cookie().value(REFRESH_TOKEN, notNullValue()))
    }

    @DisplayName("인증 새로고침 - Unauthorized")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun refreshAuthException() {
        val cookie = Cookie(REFRESH_TOKEN, UUID.randomUUID().toString())

        mockMvc.perform(
            post("$endPoint/refresh-auth")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isUnauthorized)
            .andExpect(cookie().value(ACCESS_TOKEN, emptyOrNullString()))
            .andExpect(jsonPath("message", `is`(ErrorCode.UNAUTHORIZED.message)))
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
            .andExpect(cookie().value(REFRESH_TOKEN, ""))
    }
}