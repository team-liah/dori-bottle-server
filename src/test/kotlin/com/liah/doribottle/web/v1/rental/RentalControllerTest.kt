package com.liah.doribottle.web.v1.rental

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.SAVE_PAY
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.rental.vm.RentRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
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

class RentalControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/rental"

    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var machineRepository: MachineRepository
    @Autowired private lateinit var cupRepository: CupRepository
    @Autowired private lateinit var pointRepository: PointRepository

    private lateinit var user: User
    private lateinit var guest: User
    private lateinit var vendingMachine: Machine
    private lateinit var collectionMachine: Machine
    private lateinit var cup: Cup

    @BeforeEach
    internal fun init() {
        user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        guest = userRepository.save(User(GUEST_LOGIN_ID, "사용자", GUEST_LOGIN_ID, Role.GUEST))

        val machineEntity = Machine("1", "name", VENDING, Address("12345", "test"), 100)
        machineEntity.updateCupAmounts(100)
        vendingMachine = machineRepository.save(machineEntity)
        collectionMachine = machineRepository.save(Machine("2", "name", COLLECTION, Address("12345", "test"), 100))

        cup = cupRepository.save(Cup(CUP_RFID))
    }

    @AfterEach
    internal fun destroy() {
        rentalRepository.deleteAll()
        pointRepository.deleteAll()
        userRepository.deleteAll()
        machineRepository.deleteAll()
        cupRepository.deleteAll()
    }

    @DisplayName("컵 대여")
    @Test
    fun rent() {
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)
        val body = RentRequest(vendingMachine.id, cup.rfid, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("컵 대여 - 포인트 부족")
    @Test
    fun rentExceptionLackOfPoint() {
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)
        val body = RentRequest(vendingMachine.id, cup.rfid, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.LACK_OF_POINT.message)))
    }

    @DisplayName("얼읍컵 대여 - Unauthorized")
    @Test
    fun rentExceptionFromGuest() {
        val cookie = createAccessTokenCookie(guest.id, guest.loginId, guest.role)
        val body = RentRequest(vendingMachine.id, cup.rfid, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("대여 내역 조회")
    @Test
    fun getAll() {
        insertRentals()

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.role)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("status", "PROCEEDING")
        params.add("page", "0")
        params.add("size", "3")

        val expectUserId = listOf(user.id.toString(), user.id.toString(), user.id.toString())
        val expectFromMachineId = listOf(vendingMachine.id.toString(), vendingMachine.id.toString(), vendingMachine.id.toString())
        val expectStatus = listOf(PROCEEDING.toString(), PROCEEDING.toString(), PROCEEDING.toString())

        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].userId", `is`(expectUserId)))
            .andExpect(jsonPath("content[*].fromMachine.id", `is`(expectFromMachineId)))
            .andExpect(jsonPath("content[*].status", `is`(expectStatus)))
    }

    private fun insertRentals() {
        val cup1 = cupRepository.save(Cup("B1:B1:B1:B1"))
        val cup2 = cupRepository.save(Cup("C1:C1:C1:C1"))
        val cup3 = cupRepository.save(Cup("D1:D1:D1:D1"))
        val cup4 = cupRepository.save(Cup("E1:E1:E1:E1"))
        val cup5 = cupRepository.save(Cup("F1:F1:F1:F1"))
        val cup6 = cupRepository.save(Cup("G1:G1:G1:G1"))
        rentalRepository.save(Rental(user, cup1, vendingMachine, true, 7))
        rentalRepository.save(Rental(user, cup2, vendingMachine, true, 7))
        rentalRepository.save(Rental(user, cup3, vendingMachine, true, 7))
        rentalRepository.save(Rental(user, cup4, vendingMachine, true, 7))
        rentalRepository.save(Rental(user, cup5, vendingMachine, true, 7))
        rentalRepository.save(Rental(user, cup6, vendingMachine, true, 7))
    }
}