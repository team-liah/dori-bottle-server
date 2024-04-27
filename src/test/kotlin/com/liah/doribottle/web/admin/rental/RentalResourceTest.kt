package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class RentalResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/rental"

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var machineRepository: MachineRepository

    @Autowired
    private lateinit var cupRepository: CupRepository

    @Autowired
    private lateinit var rentalRepository: RentalRepository

    @AfterEach
    internal fun destroy() {
        rentalRepository.deleteAll()
        userRepository.deleteAll()
        machineRepository.deleteAll()
        cupRepository.deleteAll()
    }

    @DisplayName("대여 내역 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        // given
        val userA = userRepository.save(User("010-1111-1111", "Tester A", "010-1111-1111", Role.USER))
        val userB = userRepository.save(User("010-2222-2222", "Tester B", "010-2222-2222", Role.USER))
        val userC = userRepository.save(User("010-3333-3333", "Tester C", "010-3333-3333", Role.USER))
        val vendingMachine = Machine("1", "name", VENDING, Address("12345", "test"), Location(37.508855, 127.059479), 100, null)
        vendingMachine.updateCupAmounts(100)
        machineRepository.save(vendingMachine)
        insertRentals(userA, userB, userC, vendingMachine)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("status", "CONFIRMED")
        params.add("fromMachineId", vendingMachine.id.toString())
        params.add("page", "0")
        params.add("size", "5")

        val expectUserId = listOf(userC.id.toString(), userC.id.toString(), userB.id.toString(), userB.id.toString(), userA.id.toString())
        val expectFromMachineId =
            listOf(
                vendingMachine.id.toString(),
                vendingMachine.id.toString(),
                vendingMachine.id.toString(),
                vendingMachine.id.toString(),
                vendingMachine.id.toString(),
            )
        val expectStatus =
            listOf(
                RentalStatus.CONFIRMED.toString(),
                RentalStatus.CONFIRMED.toString(),
                RentalStatus.CONFIRMED.toString(),
                RentalStatus.CONFIRMED.toString(),
                RentalStatus.CONFIRMED.toString(),
            )

        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].user.id", `is`(expectUserId)))
            .andExpect(jsonPath("content[*].fromMachine.id", `is`(expectFromMachineId)))
            .andExpect(jsonPath("content[*].status", `is`(expectStatus)))
    }

    private fun insertRentals(
        userA: User,
        userB: User,
        userC: User,
        vendingMachine: Machine,
    ) {
        rentalRepository.save(Rental(userA, cupRepository.save(Cup("B1:B1:B1:B1")), vendingMachine, true, 7))
        rentalRepository.save(Rental(userA, cupRepository.save(Cup("C1:C1:C1:C1")), vendingMachine, true, 7))
        rentalRepository.save(Rental(userB, cupRepository.save(Cup("D1:D1:D1:D1")), vendingMachine, true, 7))
        rentalRepository.save(Rental(userB, cupRepository.save(Cup("E1:E1:E1:E1")), vendingMachine, true, 7))
        rentalRepository.save(Rental(userC, cupRepository.save(Cup("F1:F1:F1:F1")), vendingMachine, true, 7))
        rentalRepository.save(Rental(userC, cupRepository.save(Cup("G1:G1:G1:G1")), vendingMachine, true, 7))
    }

    @DisplayName("대여 내역 단건 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        // given
        val user = userRepository.save(User("010-1111-1111", "Tester", "010-1111-1111", Role.USER))
        val vendingMachine = Machine("1", "name", VENDING, Address("12345", "test"), Location(37.508855, 127.059479), 100, null)
        vendingMachine.updateCupAmounts(100)
        machineRepository.save(vendingMachine)
        val rental = rentalRepository.save(Rental(user, cupRepository.save(Cup("B1:B1:B1:B1")), vendingMachine, true, 7))

        mockMvc.perform(
            get("$endPoint/${rental.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("user.id", `is`(user.id.toString())))
            .andExpect(jsonPath("fromMachine.id", `is`(vendingMachine.id.toString())))
            .andExpect(jsonPath("status", `is`(rental.status.name)))
    }

    @DisplayName("반납")
    @WithMockDoriUser(loginId = MACHINE_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun `return`() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val vendingMachine =
            machineRepository.save(
                Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null),
            )
        val collectionMachine =
            machineRepository.save(
                Machine("0000002", "name", COLLECTION, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null),
            )
        val cup = cupRepository.save(Cup(CUP_RFID))
        rentalRepository.save(Rental(user, cup, vendingMachine, true, 14))

        val body = ReturnRequest(collectionMachine.no, CUP_RFID)
        mockMvc.perform(
            post("$endPoint/return")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("대여 취소")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun cancel() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val vendingMachine =
            machineRepository.save(
                Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null),
            )
        val collectionMachine =
            machineRepository.save(
                Machine("0000002", "name2", COLLECTION, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 0, null),
            )
        val cup = cupRepository.save(Cup(CUP_RFID))
        val rental = rentalRepository.save(Rental(user, cup, vendingMachine, true, 14))
        rental.`return`(collectionMachine)
        rentalRepository.save(rental)

        mockMvc.perform(
            post("$endPoint/${rental.id}/cancel")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("대여 통계 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getStatistic() {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("year", "2024")
        params.add("month", "APRIL")

        mockMvc.perform(
            get("$endPoint/statistic")
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
    }
}
