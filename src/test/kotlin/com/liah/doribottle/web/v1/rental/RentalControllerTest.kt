package com.liah.doribottle.web.v1.rental

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType
import com.liah.doribottle.domain.payment.PaymentMethodType
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.SAVE_PAY
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus.CONFIRMED
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
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
import java.time.Instant

class RentalControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/rental"

    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var machineRepository: MachineRepository
    @Autowired private lateinit var cupRepository: CupRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var paymentMethodRepository: PaymentMethodRepository

    private lateinit var user: User
    private lateinit var guest: User
    private lateinit var vendingMachine: Machine
    private lateinit var collectionMachine: Machine
    private lateinit var cup: Cup

    @BeforeEach
    internal fun init() {
        user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        guest = userRepository.save(User(GUEST_LOGIN_ID, "사용자", GUEST_LOGIN_ID, Role.GUEST))

        val card = Card(CardProvider.HYUNDAI, CardProvider.HYUNDAI, "1234", CardType.CREDIT, CardOwnerType.PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, "key", PaymentMethodProviderType.TOSSPAYMENTS, PaymentMethodType.CARD, card, true, Instant.now()))

        val machineEntity = Machine("1", "name", VENDING, Address("12345", "test"), Location(37.508855, 127.059479), 100)
        machineEntity.updateCupAmounts(100)
        vendingMachine = machineRepository.save(machineEntity)
        collectionMachine = machineRepository.save(Machine("2", "name", COLLECTION, Address("12345", "test"), Location(37.508855, 127.059479), 100))

        cup = cupRepository.save(Cup(CUP_RFID))
    }

    @AfterEach
    internal fun destroy() {
        paymentMethodRepository.deleteAll()
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

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = RentRequest(vendingMachine.no, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("컵 대여 예외 - 포인트 부족")
    @Test
    fun rentExceptionLackOfPoint() {
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = RentRequest(vendingMachine.no, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.LACK_OF_POINT.message)))
    }

    @DisplayName("컵 대여 예외 - 블락 유저")
    @Test
    fun rentExceptionBlockedUser() {
        val user = User("010-0001-0001", "Tester 1", "010-0001-0001", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = RentRequest(vendingMachine.no, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("code", `is`(ErrorCode.BLOCKED_USER_ACCESS_DENIED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.BLOCKED_USER_ACCESS_DENIED.message)))
    }

    @DisplayName("컵 대여 예외 - 결제수단 미등록")
    @Test
    fun rentExceptionPaymentMethodNotFound() {
        val user = userRepository.save(User("010-0001-0001", "Tester 2", "010-0001-0001", Role.USER))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = RentRequest(vendingMachine.no, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("code", `is`(ErrorCode.PAYMENT_METHOD_NOT_FOUND.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.PAYMENT_METHOD_NOT_FOUND.message)))
    }

    @DisplayName("얼읍컵 대여 - Unauthorized")
    @Test
    fun rentExceptionFromGuest() {
        val cookie = createAccessTokenCookie(guest.id, guest.loginId, guest.name, guest.role)
        val body = RentRequest(vendingMachine.no, true)

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("대여 내역 조회")
    @Test
    fun getAll() {
        insertRentals()

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("status", "CONFIRMED")
        params.add("page", "0")
        params.add("size", "3")

        val expectUserId = listOf(user.id.toString(), user.id.toString(), user.id.toString())
        val expectFromMachineId = listOf(vendingMachine.id.toString(), vendingMachine.id.toString(), vendingMachine.id.toString())
        val expectStatus = listOf(CONFIRMED.toString(), CONFIRMED.toString(), CONFIRMED.toString())

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
        val rental1 = Rental(user, vendingMachine, true, 7)
        rental1.confirm(cupRepository.save(Cup("B1:B1:B1:B1")))
        rentalRepository.save(rental1)

        val rental2 = Rental(user, vendingMachine, true, 7)
        rental2.confirm(cupRepository.save(Cup("C1:C1:C1:C1")))
        rentalRepository.save(rental2)

        val rental3 = Rental(user, vendingMachine, true, 7)
        rental3.confirm(cupRepository.save(Cup("D1:D1:D1:D1")))
        rentalRepository.save(rental3)

        val rental4 = Rental(user, vendingMachine, true, 7)
        rental4.confirm(cupRepository.save(Cup("E1:E1:E1:E1")))
        rentalRepository.save(rental4)

        val rental5 = Rental(user, vendingMachine, true, 7)
        rental5.confirm(cupRepository.save(Cup("F1:F1:F1:F1")))
        rentalRepository.save(rental5)

        val rental6 = Rental(user, vendingMachine, true, 7)
        rental6.confirm(cupRepository.save(Cup("G1:G1:G1:G1")))
        rentalRepository.save(rental6)
    }
}