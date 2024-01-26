package com.liah.doribottle.web.admin.machine

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.machine.vm.MachinePatchRequest
import com.liah.doribottle.web.admin.machine.vm.MachineRegisterRequest
import com.liah.doribottle.web.admin.machine.vm.MachineUpdateRequest
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

class MachineResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/machine"

    @Autowired
    private lateinit var machineRepository: MachineRepository

    @AfterEach
    internal fun destroy() {
        machineRepository.deleteAll()
    }

    @DisplayName("자판기 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = MachineRegisterRequest("0000001", "name", VENDING, AddressDto("12345", "삼성로"), LocationDto(37.508855, 127.059479), 100, null)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("자판기 등록 - 예외")
    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @Test
    fun registerException() {
        val body = MachineRegisterRequest("0000001", "name", VENDING, AddressDto("12345", "삼성로"), LocationDto(37.508855, 127.059479), 100, null)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("자판기 등록 - 예외 TC2")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun registerExceptionTc2() {
        machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        val body = MachineRegisterRequest("0000001", "name", VENDING, AddressDto("12345", "삼성로"), LocationDto(37.508855, 127.059479), 100, null)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.MACHINE_ALREADY_REGISTERED.message)))
    }

    @DisplayName("자판기 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))

        mockMvc.perform(
            get("${endPoint}/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("no", `is`(machine.no)))
            .andExpect(jsonPath("name", `is`(machine.name)))
            .andExpect(jsonPath("type", `is`(machine.type.name)))
            .andExpect(jsonPath("address.zipCode", `is`(machine.address.zipCode)))
            .andExpect(jsonPath("address.address1", `is`(machine.address.address1)))
            .andExpect(jsonPath("address.address2", `is`(machine.address.address2)))
            .andExpect(jsonPath("location.latitude", `is`(37.508855)))
            .andExpect(jsonPath("location.longitude", `is`(127.059479)))
            .andExpect(jsonPath("capacity", `is`(machine.capacity)))
            .andExpect(jsonPath("cupAmounts", `is`(machine.cupAmounts)))
            .andExpect(jsonPath("state", `is`(machine.state.name)))
    }

    @DisplayName("자판기 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertMachines()

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

    private fun insertMachines() {
        machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479),  100, null))
        machineRepository.save(Machine("0000002", "name", VENDING, Address("00002", "삼성로", null), Location(37.508855, 127.059479),  100, null))
        machineRepository.save(Machine("0000003", "name", VENDING, Address("00003", "삼성로", null), Location(37.508855, 127.059479),  100, null))
        machineRepository.save(Machine("0000004", "name", VENDING, Address("00004", "마장로", null), Location(37.508855, 127.059479),  100, null))
        machineRepository.save(Machine("0000005", "name", COLLECTION, Address("00005", "도산대로", null), Location(37.508855, 127.059479),  100, null))
        machineRepository.save(Machine("0000006", "name", VENDING, Address("00006", "도산대로", null), Location(37.508855, 127.059479),  100, null))
    }

    @DisplayName("자판기 정보 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        val body = MachineUpdateRequest("name", AddressDto("12345", "삼성로"), LocationDto(37.508855, 127.059479), 100, 50, NORMAL, null)

        mockMvc.perform(
            put("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("자판기 정보 수정 - 예외 TC2")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun updateExceptionTc2() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        val body = MachineUpdateRequest("name", AddressDto("12345", "삼성로"), LocationDto(37.508855, 127.059479), 100, -1, NORMAL, null)

        mockMvc.perform(
            put("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("code", `is`(ErrorCode.INVALID_INPUT_VALUE.code)))
    }

    @DisplayName("자판기 정보 패치")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun patch() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        val body = MachinePatchRequest("updated",  null, null, null, 10, null, null)

        mockMvc.perform(
            patch("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("자판기 정보 패치 POST")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun patchPost() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        val body = MachinePatchRequest("updated",  null, null, null, 10, null, null)

        mockMvc.perform(
            post("$endPoint/${machine.id}/patch")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("기기 삭제")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun delete() {
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))

        mockMvc.perform(
            delete("$endPoint/${machine.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }
}