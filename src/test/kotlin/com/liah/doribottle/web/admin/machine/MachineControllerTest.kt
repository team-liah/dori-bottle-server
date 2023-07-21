package com.liah.doribottle.web.admin.machine

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.web.admin.machine.vm.MachineRegisterRequest
import com.liah.doribottle.web.admin.machine.vm.MachineUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.context.WebApplicationContext

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MachineControllerTest {
    private lateinit var mockMvc: MockMvc
    private val endPoint = "/admin/api/machine"

    companion object {
        private const val ADMIN_LOGIN_ID = "admin"
    }

    @Autowired
    private lateinit var context: WebApplicationContext

    @Autowired
    private lateinit var machineRepository: MachineRepository

    @BeforeEach
    internal fun setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply<DefaultMockMvcBuilder?>(SecurityMockMvcConfigurers.springSecurity())
            .build()
    }

    @AfterEach
    internal fun destroy() {
        machineRepository.deleteAll()
    }

    @DisplayName("자판기 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = MachineRegisterRequest("0000001", "name", VENDING, AddressDto("12345", "삼성로"), 100)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("자판기 등록 - 예외")
    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @Test
    fun registerException() {
        val body = MachineRegisterRequest("0000001", "name", VENDING, AddressDto("12345", "삼성로"), 100)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("자판기 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        machineRepository.save(Machine("0000002", "name", VENDING, Address("00002", "삼성로", null), 100))
        machineRepository.save(Machine("0000003", "name", VENDING, Address("00003", "삼성로", null), 100))
        machineRepository.save(Machine("0000004", "name", VENDING, Address("00004", "마장로", null), 100))
        machineRepository.save(Machine("0000005", "name", COLLECTION, Address("00005", "도산대로", null), 100))
        machineRepository.save(Machine("0000006", "name", VENDING, Address("00006", "도산대로", null), 100))


        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("type", "VENDING")
        params.add("page", "0")
        params.add("size", "3")

        val expectValue = listOf("0000006", "0000004", "0000003")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].no", `is`(expectValue)))
    }

    @DisplayName("자판기 정보 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val body = MachineUpdateRequest("name", AddressDto("12345", "삼성로"), 100, 50)

        mockMvc.perform(
            put("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("자판기 정보 수정 - 예외")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun updateException() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val body = MachineUpdateRequest("name", AddressDto("12345", "삼성로"), 100, 110)

        mockMvc.perform(
            put("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.FULL_OF_CUP.message)))
    }
}