package com.liah.doribottle.web.v1.machine

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MachineControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/machine"

    @Autowired
    private lateinit var machineRepository: MachineRepository

    @AfterEach
    internal fun destroy() {
        machineRepository.deleteAll()
    }

    @DisplayName("기기 전체 조회")
    @Test
    fun getAll() {
        //given
        insertMachines()

        //when, then
        val expectType = listOf(MachineType.VENDING.name, MachineType.VENDING.name, MachineType.VENDING.name, MachineType.VENDING.name, MachineType.COLLECTION.name, MachineType.VENDING.name)
        val expectLatitude = listOf(37.508855, 37.508955, 37.508355, 37.508455, 37.518855, 37.503855)
        val expectLongitude = listOf(127.059479, 127.052479, 127.051479, 127.053479, 127.029479, 127.059179)
        mockMvc.perform(
            get("${endPoint}/all")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("[*].type", `is`(expectType)))
            .andExpect(jsonPath("[*].location.latitude", `is`(expectLatitude)))
            .andExpect(jsonPath("[*].location.longitude", `is`(expectLongitude)))
    }

    @DisplayName("기기 조회")
    @Test
    fun get() {
        val machine = machineRepository.save(Machine("0000001", "name", MachineType.VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100))

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

    private fun insertMachines() {
        machineRepository.save(Machine("0000001", "name", MachineType.VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479),  100))
        machineRepository.save(Machine("0000002", "name", MachineType.VENDING, Address("00002", "삼성로", null), Location(37.508955, 127.052479),  100))
        machineRepository.save(Machine("0000003", "name", MachineType.VENDING, Address("00003", "삼성로", null), Location(37.508355, 127.051479),  100))
        machineRepository.save(Machine("0000004", "name", MachineType.VENDING, Address("00004", "마장로", null), Location(37.508455, 127.053479),  100))
        machineRepository.save(Machine("0000005", "name", MachineType.COLLECTION, Address("00005", "도산대로", null), Location(37.518855, 127.029479),  100))
        machineRepository.save(Machine("0000006", "name", MachineType.VENDING, Address("00006", "도산대로", null), Location(37.503855, 127.059179),  100))
    }
}