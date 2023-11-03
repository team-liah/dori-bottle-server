package com.liah.doribottle.web.admin.me

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class AdminMeControllerTest : BaseControllerTest() {
    private val endPoint = "/admin/api/me"

    @DisplayName("현재 로그인 관리자 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        mockMvc.perform(
            get(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("loginId", `is`(ADMIN_LOGIN_ID)))
            .andExpect(jsonPath("name", `is`("MockDoriUser")))
            .andExpect(jsonPath("role", `is`(Role.ADMIN.name)))
    }
}