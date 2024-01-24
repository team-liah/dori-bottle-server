package com.liah.doribottle.web.admin.admin

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.extension.systemId
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.user.AdminService
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.admin.vm.AdminPasswordUpdateRequest
import com.liah.doribottle.web.admin.admin.vm.AdminRegisterRequest
import com.liah.doribottle.web.admin.admin.vm.AdminUpdateRequest
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.notNullValue
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
    @Autowired
    private lateinit var adminService: AdminService

    @AfterEach
    internal fun destroy() {
        adminRepository.deleteAll()
    }

    @DisplayName("관리자 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        //given
        val body = AdminRegisterRequest(ADMIN_LOGIN_ID, "1234", "admin", Role.ADMIN, null, null, null)

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
        val body = AdminRegisterRequest(ADMIN_LOGIN_ID, "1234", "admin", Role.ADMIN, null, null, null)

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
        adminRepository.save(Admin("admin1", "123456", "Tester 1", Role.ADMIN, null, null, null))
        adminRepository.save(Admin("admin2", "123456", "Tester 2", Role.MACHINE_ADMIN, null, null, null))
        adminRepository.save(Admin("admin3", "123456", "Tester 3", Role.INSTITUTION, null, null, null))
        adminRepository.save(Admin("admin4", "123456", "Tester 4", Role.MACHINE_ADMIN, null, null, null))
        adminRepository.save(Admin("admin5", "123456", "Tester 5", Role.ADMIN, null, null, null))
        adminRepository.save(Admin("admin6", "123456", "Tester 6", Role.ADMIN, null, null, null))
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
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN, null, null, null))
        val body = AdminUpdateRequest("updated", "1234", null, null, null)

        mockMvc.perform(
            put("$endPoint/${admin.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("관리자 비밀번호 변경")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun updatePassword() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN, null, null, null))
        val body = AdminPasswordUpdateRequest("updated")

        mockMvc.perform(
            put("$endPoint/${admin.id}/password")
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
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN, null, null, null))

        mockMvc.perform(
            delete("$endPoint/${admin.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("시스템 토큰 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getSystemToken() {
        adminService.register(systemId(), "system", "system", "system", Role.SYSTEM)

        mockMvc.perform(
            get("$endPoint/system-token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("systemToken", notNullValue()))
    }
}