package com.liah.doribottle.web.admin.cup

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.cup.vm.CupRegisterRequest
import com.liah.doribottle.web.admin.cup.vm.CupUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class CupResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/cup"

    @Autowired
    private lateinit var cupRepository: CupRepository


    @AfterEach
    internal fun destroy() {
        cupRepository.deleteAll()
    }

    @DisplayName("컵 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = CupRegisterRequest("A1:A1:A1:A1")

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("컵 등록 - 예외")
    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @Test
    fun registerException() {
        val body = CupRegisterRequest("A1:A1:A1:A1")

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("컵 등록 - 예외 TC2")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun registerExceptionTc2() {
        cupRepository.save(Cup("A1:A1:A1:A1"))
        val body = CupRegisterRequest("A1:A1:A1:A1")

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.CUP_ALREADY_REGISTERED.message)))
    }

    @DisplayName("컵 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertCups()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("type", "AVAILABLE")
        params.add("page", "0")
        params.add("size", "3")

        val expectValue = listOf("F1:F1:F1:F1", "E1:E1:E1:E1", "D1:D1:D1:D1")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].rfid", `is`(expectValue)))
    }

    private fun insertCups() {
        cupRepository.save(Cup("A1:A1:A1:A1"))
        cupRepository.save(Cup("B1:B1:B1:B1"))
        cupRepository.save(Cup("C1:C1:C1:C1"))
        cupRepository.save(Cup("D1:D1:D1:D1"))
        cupRepository.save(Cup("E1:E1:E1:E1"))
        cupRepository.save(Cup("F1:F1:F1:F1"))
    }

    @DisplayName("컵 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val cup = cupRepository.save(Cup("A1:A1:A1:A1"))
        mockMvc.perform(
            get("${endPoint}/${cup.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("rfid", `is`("A1:A1:A1:A1")))
            .andExpect(jsonPath("status", `is`(CupStatus.AVAILABLE.name)))
    }

    @DisplayName("컵 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val cup = cupRepository.save(Cup("A1:A1:A1:A1"))
        val body = CupUpdateRequest("B1:B1:B1:B1", CupStatus.LOST)

        mockMvc.perform(
            put("${endPoint}/${cup.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("컵 삭제")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun remove() {
        val cup = cupRepository.save(Cup("A1:A1:A1:A1"))

        mockMvc.perform(
            delete("${endPoint}/${cup.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }
}