package com.liah.doribottle.web.admin.admin

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.admin.vm.AdminRegisterOrUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class AdminResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/admin"

    @Autowired
    private lateinit var adminRepository: AdminRepository

    @AfterEach
    internal fun destroy() {
        adminRepository.deleteAll()
    }

    @DisplayName("관리자 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        //given
        val body = AdminRegisterOrUpdateRequest(ADMIN_LOGIN_ID, "1234", "admin", Role.ADMIN)

        //when, then
        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("관리자 등록 예외")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun registerException() {
        //given
        val body = AdminRegisterOrUpdateRequest(ADMIN_LOGIN_ID, "1234", "admin", Role.ADMIN)

        //when, then
        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isForbidden)
    }

    @DisplayName("관리자 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertAdmins()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("role", "MACHINE_ADMIN")
        params.add("page", "0")
        params.add("size", "3")

        val expectValue = listOf("Tester 4", "Tester 2")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].name", `is`(expectValue)))
    }

    private fun insertAdmins() {
        adminRepository.save(Admin("admin1", "123456", "Tester 1", Role.ADMIN))
        adminRepository.save(Admin("admin2", "123456", "Tester 2", Role.MACHINE_ADMIN))
        adminRepository.save(Admin("admin3", "123456", "Tester 3", Role.INSTITUTION))
        adminRepository.save(Admin("admin4", "123456", "Tester 4", Role.MACHINE_ADMIN))
        adminRepository.save(Admin("admin5", "123456", "Tester 5", Role.ADMIN))
        adminRepository.save(Admin("admin6", "123456", "Tester 6", Role.ADMIN))
    }

    @DisplayName("관리자 목록 조회 예외")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun getAllException() {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }

    @DisplayName("관리자 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        val body = AdminRegisterOrUpdateRequest("updated", "1234", "updated", Role.MACHINE_ADMIN)

        mockMvc.perform(
            put("$endPoint/${admin.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("관리자 삭제")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun delete() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))

        mockMvc.perform(
            delete("$endPoint/${admin.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }
}