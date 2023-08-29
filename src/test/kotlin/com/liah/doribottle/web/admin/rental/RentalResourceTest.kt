package com.liah.doribottle.web.admin.rental

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.rental.vm.RentalCupUpdateRequest
import com.liah.doribottle.web.admin.rental.vm.ReturnRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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

    @DisplayName("대여 컵 업데이트")
    @WithMockDoriUser(loginId = MACHINE_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun updateRentalCup() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val vendingMachine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val cup = cupRepository.save(Cup(CUP_RFID))
        val rental = rentalRepository.save(Rental(user, vendingMachine, true, 14))
        val body = RentalCupUpdateRequest(cup.rfid)

        mockMvc.perform(
            MockMvcRequestBuilders.put("${endPoint}/${rental.id}/cup")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("반납")
    @WithMockDoriUser(loginId = MACHINE_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun `return`() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val vendingMachine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val collectionMachine = machineRepository.save(Machine("0000002", "name", COLLECTION, Address("00001", "삼성로", null), 100))
        val cup = cupRepository.save(Cup(CUP_RFID))
        val rental = Rental(user, vendingMachine, true, 14)
        rental.setRentalCup(cup)
        rentalRepository.save(rental)
        cupRepository.save(cup)

        val body = ReturnRequest(collectionMachine.no, CUP_RFID)
        mockMvc.perform(
            post("${endPoint}/return")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }
}