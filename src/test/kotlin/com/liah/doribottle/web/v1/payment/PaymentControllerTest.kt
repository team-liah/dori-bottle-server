package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.PaymentCancelException
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.payment.*
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.TOSS_PAYMENTS
import com.liah.doribottle.domain.payment.PaymentMethodType.CARD
import com.liah.doribottle.domain.payment.PaymentStatus.CANCELED
import com.liah.doribottle.domain.payment.PaymentStatus.SUCCEEDED
import com.liah.doribottle.domain.payment.PaymentType.*
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType.CORPORATE
import com.liah.doribottle.domain.payment.card.CardOwnerType.PERSONAL
import com.liah.doribottle.domain.payment.card.CardProvider.*
import com.liah.doribottle.domain.payment.card.CardType.CREDIT
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.payment.PaymentRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.BlockedCauseRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.CardDto
import com.liah.doribottle.service.payment.dto.PaymentResultDto
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.payment.vm.PayToSavePointRequest
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodRegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class PaymentControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/payment"

    @Autowired
    private lateinit var paymentRepository: PaymentRepository
    @Autowired
    private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var groupRepository: GroupRepository
    @Autowired
    private lateinit var pointRepository: PointRepository
    @Autowired
    private lateinit var blockedCauseRepository: BlockedCauseRepository
    @Autowired
    private lateinit var rentalRepository: RentalRepository
    @Autowired
    private lateinit var machineRepository: MachineRepository
    @Autowired
    private lateinit var cupRepository: CupRepository

    @MockBean
    private lateinit var mockTossPaymentsService: TossPaymentsService

    @AfterEach
    internal fun destroy() {
        rentalRepository.deleteAll()
        machineRepository.deleteAll()
        cupRepository.deleteAll()
        paymentRepository.deleteAll()
        pointRepository.deleteAll()
        paymentMethodRepository.deleteAll()
        paymentCategoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("포인트 충전 결제")
    @Test
    fun payToSavePoint() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val billingKey = "dummyBillingKey"
        val paymentKey = "dummyPaymentKey"
        paymentMethodRepository.save(PaymentMethod(user,billingKey, TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), true, Instant.now()))

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(900L), any<UUID>(), eq(SAVE_POINT)))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, null))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = PayToSavePointRequest(category.id)

        //when, then
        mockMvc.perform(
            post("${endPoint}/save-point")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(900L), any<UUID>(), eq(SAVE_POINT))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findPoint = pointRepository.findAll().firstOrNull()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(900)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.number).isEqualTo("12341234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(SUCCEEDED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo(paymentKey)
        assertThat(findPayment?.point?.id).isEqualTo(findPoint?.id!!)

        assertThat(findPoint.saveAmounts).isEqualTo(category.amounts)
    }

    @DisplayName("포인트 충전 결제 TC2: 기관 할인")
    @Test
    fun payToSavePointTc2() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        val group = groupRepository.save(Group("Group", GroupType.UNIVERSITY, 20))
        user.updateGroup(group)
        userRepository.save(user)
        val billingKey = "dummyBillingKey"
        val paymentKey = "dummyPaymentKey"
        paymentMethodRepository.save(PaymentMethod(user,billingKey, TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), true, Instant.now()))

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(720L), any<UUID>(), eq(SAVE_POINT)))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, null))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = PayToSavePointRequest(category.id)

        //when, then
        mockMvc.perform(
            post("${endPoint}/save-point")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(720L), any<UUID>(), eq(SAVE_POINT))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findPoint = pointRepository.findAll().firstOrNull()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(720)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.number).isEqualTo("12341234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(SUCCEEDED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo(paymentKey)
        assertThat(findPayment?.point?.id).isEqualTo(findPoint?.id!!)

        assertThat(findPoint.saveAmounts).isEqualTo(category.amounts)
    }

    @DisplayName("포인트 충전 결제 예외")
    @Test
    fun payToSavePointException() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val billingKey = "dummyBillingKey"
        paymentMethodRepository.save(PaymentMethod(user, billingKey, TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), true, Instant.now()))

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(900L), any<UUID>(), eq(SAVE_POINT)))
            .willThrow(BillingExecuteException())

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = PayToSavePointRequest(category.id)

        //when, then
        mockMvc.perform(
            post("${endPoint}/save-point")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().is5xxServerError)
            .andExpect(jsonPath("code", `is`(ErrorCode.BILLING_EXECUTE_ERROR.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.BILLING_EXECUTE_ERROR.message)))

        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(900L), any<UUID>(), eq(SAVE_POINT))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findPoint = pointRepository.findAll().firstOrNull()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(900)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.number).isEqualTo("12341234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(PaymentStatus.FAILED)
        assertThat(findPayment?.result).isNull()
        assertThat(findPayment?.point).isNull()

        assertThat(findPoint).isNull()
    }

    @DisplayName("계정 블락 해제 결제")
    @Test
    fun payToUnblockAccount() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        user.block(BlockedCauseType.FIVE_PENALTIES, null)
        userRepository.save(user)

        val billingKey = "dummyBillingKey"
        val paymentKey = "dummyPaymentKey"
        paymentMethodRepository.save(PaymentMethod(user,billingKey, TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), true, Instant.now()))

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(40000L), any<UUID>(), eq(UNBLOCK_ACCOUNT)))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, null))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            post("${endPoint}/unblock-account")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(40000L), any<UUID>(), eq(UNBLOCK_ACCOUNT))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findUser = userRepository.findByIdOrNull(user.id)
        val findBlockedCauses = blockedCauseRepository.findAll()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(40000)
        assertThat(findPayment?.type).isEqualTo(UNBLOCK_ACCOUNT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.number).isEqualTo("12341234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(SUCCEEDED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo(paymentKey)

        assertThat(findUser?.blocked).isFalse()
        assertThat(findBlockedCauses).isEmpty()
    }

    @DisplayName("계정 블락 해제 결제 예외")
    @Test
    fun payToUnblockAccountException() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        user.block(BlockedCauseType.FIVE_PENALTIES, null)
        userRepository.save(user)

        val billingKey = "dummyBillingKey"
        paymentMethodRepository.save(PaymentMethod(user,billingKey, TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), true, Instant.now()))

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(40000L), any<UUID>(), eq(UNBLOCK_ACCOUNT)))
            .willThrow(BillingExecuteException())

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            post("${endPoint}/unblock-account")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is5xxServerError)
            .andExpect(jsonPath("code", `is`(ErrorCode.BILLING_EXECUTE_ERROR.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.BILLING_EXECUTE_ERROR.message)))

        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(40000L), any<UUID>(), eq(UNBLOCK_ACCOUNT))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findUser = userRepository.findByIdOrNull(user.id)
        val findBlockedCauses = blockedCauseRepository.findAll()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(40000)
        assertThat(findPayment?.type).isEqualTo(UNBLOCK_ACCOUNT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(findPayment?.card?.number).isEqualTo("12341234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(PaymentStatus.FAILED)
        assertThat(findPayment?.result).isNull()

        assertThat(findUser?.blocked).isTrue()
        assertThat(findBlockedCauses)
            .extracting("type")
            .containsExactly(
                BlockedCauseType.LOST_CUP_PENALTY,
                BlockedCauseType.LOST_CUP_PENALTY,
                BlockedCauseType.FIVE_PENALTIES
            )
    }

    @DisplayName("결제 내역 조회")
    @Test
    fun getAll() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        insertPayments(user)
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectUserId = listOf(user.id.toString(), user.id.toString(), user.id.toString())
        val expectPrice = listOf(6000, 5000, 4000)
        val expectType = listOf(SAVE_POINT.name, LOST_CUP.name, SAVE_POINT.name)
        val expectStatus = listOf(CANCELED.name, SUCCEEDED.name, SUCCEEDED.name)

        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].userId", `is`(expectUserId)))
            .andExpect(jsonPath("content[*].price", `is`(expectPrice)))
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].status", `is`(expectStatus)))
    }

    private fun insertPayments(user: User) {
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val payment1 = Payment(user, 1000, SAVE_POINT, card)
        val point1 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))
        val result1 = PaymentResult("dummyPaymentKey1", Instant.now(), null, null)
        payment1.updateResult(result1, point1)
        paymentRepository.save(payment1)

        val payment2 = Payment(user, 2000, LOST_CUP, card)
        val result2 = PaymentResult("dummyPaymentKey2", Instant.now(), null, null)
        payment2.updateResult(result2, null)
        paymentRepository.save(payment2)

        val payment3 = Payment(user, 3000, SAVE_POINT, card)
        val point3 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 30))
        val result3 = PaymentResult("dummyPaymentKey3", Instant.now(), null, "dummyCancelKey3")
        payment3.updateResult(result3, point3)
        paymentRepository.save(payment3)

        val payment4 = Payment(user, 4000, SAVE_POINT, card)
        val point4 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 40))
        val result4 = PaymentResult("dummyPaymentKey4", Instant.now(), null, null)
        payment4.updateResult(result4, point4)
        paymentRepository.save(payment4)

        val payment5 = Payment(user, 5000, LOST_CUP, card)
        val result5 = PaymentResult("dummyPaymentKey5", Instant.now(), null, null)
        payment5.updateResult(result5, null)
        paymentRepository.save(payment5)

        val payment6 = Payment(user, 6000, SAVE_POINT, card)
        val point6 = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 60))
        val result6 = PaymentResult("dummyPaymentKey6", Instant.now(), null, "dummyCancelKey6")
        payment6.updateResult(result6, point6)
        paymentRepository.save(payment6)
    }

    @DisplayName("포인트 충전 결제 취소")
    @Test
    fun cancel() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val point = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 30))
        val payment = Payment(user, 3000, SAVE_POINT, card)
        val paymentKey = "dummyPaymentKey"
        val cancelKey = "dummyCancelKey"
        payment.updateResult(PaymentResult(paymentKey, Instant.now(), "", null), point)
        paymentRepository.save(payment)

        given(mockTossPaymentsService.cancelPayment(eq(paymentKey), eq("포인트 적립 취소")))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, cancelKey))

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            post("${endPoint}/${payment.id}/cancel")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        verify(mockTossPaymentsService, times(1))
            .cancelPayment(eq(paymentKey), eq("포인트 적립 취소"))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findPoint = pointRepository.findAll().firstOrNull()

        assertThat(findPayment?.user?.id).isEqualTo(user.id)
        assertThat(findPayment?.price).isEqualTo(3000)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.number).isEqualTo("1234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(CANCELED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo(paymentKey)
        assertThat(findPayment?.result?.cancelKey).isEqualTo(cancelKey)
        assertThat(findPayment?.point?.id).isEqualTo(findPoint?.id!!)

        assertThat(findPoint.remainAmounts).isEqualTo(0)
    }

    @DisplayName("포인트 충전 결제 취소 예외")
    @Test
    fun cancelException() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val point = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 30))
        val payment = Payment(user, 3000, SAVE_POINT, card)
        val paymentKey = "dummyPaymentKey"
        payment.updateResult(PaymentResult(paymentKey, Instant.now(), "", null), point)
        paymentRepository.save(payment)

        given(mockTossPaymentsService.cancelPayment(eq(paymentKey), eq("포인트 적립 취소")))
            .willThrow(PaymentCancelException())

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            post("${endPoint}/${payment.id}/cancel")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().is5xxServerError)
            .andExpect(jsonPath("code", `is`(ErrorCode.PAYMENT_CANCEL_ERROR.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.PAYMENT_CANCEL_ERROR.message)))
    }

    @DisplayName("결제 수단 등록")
    @Test
    fun registerMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = PaymentMethodRegisterRequest(TOSS_PAYMENTS, "dummyAuthKey")

        given(mockTossPaymentsService.issueBillingKey("dummyAuthKey", user.id))
            .willReturn(
                BillingInfo("dummyBillingKey", TOSS_PAYMENTS, CARD, CardDto(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), Instant.now())
            )

        //when, then
        mockMvc.perform(
            post("$endPoint/method")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("결제 수단 목록 조회")
    @Test
    fun getAllMethods() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        insertMethods(user)
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectType = listOf(CARD.name, CARD.name, CARD.name)
        val expectAcquire = listOf("현대", "비씨", "국민")
        val expectNumber = listOf("6789", "5678", "4567")
        val expectDefault = listOf(false, false, false)

        mockMvc.perform(
            get("${endPoint}/method")
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].card.acquirer", `is`(expectAcquire)))
            .andExpect(jsonPath("content[*].card.number", `is`(expectNumber)))
            .andExpect(jsonPath("content[*].default", `is`(expectDefault)))
    }

    private fun insertMethods(user: User) {
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey2", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "23452345", CREDIT, PERSONAL), true, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey3", TOSS_PAYMENTS, CARD, Card(SAMSUNG, SAMSUNG, "34563456", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey4", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "45674567", CREDIT, CORPORATE), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey5", TOSS_PAYMENTS, CARD, Card(BC, BC, "56785678", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey6", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "67896789", CREDIT, PERSONAL), false, Instant.now()))
    }

    @DisplayName("기본 결제 수단 변경")
    @Test
    fun changeDefaultMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method1 = paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val method2 = paymentMethodRepository.save(PaymentMethod(user,"dummyKey2", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), false, Instant.now()))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            post("$endPoint/method/${method2.id}/default")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findMethod1 = paymentMethodRepository.findByIdOrNull(method1.id)
        val findMethod2 = paymentMethodRepository.findByIdOrNull(method2.id)
        assertThat(findMethod1?.default).isFalse()
        assertThat(findMethod2?.default).isTrue()
    }

    @DisplayName("결제 수단 제거")
    @Test
    fun removeMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), false, Instant.now()))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            delete("$endPoint/method/${method.id}")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findMethod = paymentMethodRepository.findByIdOrNull(method.id)
        assertThat(findMethod).isNull()
    }

    @DisplayName("결제 수단 제거 TC2")
    @Test
    fun removeMethodTc2() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val defaultMethod = paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val anotherMethod = paymentMethodRepository.save(PaymentMethod(user,"dummyKey2", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), false, Instant.now()))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            delete("$endPoint/method/${defaultMethod.id}")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findDefaultMethod = paymentMethodRepository.findByIdOrNull(defaultMethod.id)
        val findAnotherMethod = paymentMethodRepository.findByIdOrNull(anotherMethod.id)
        assertThat(findDefaultMethod).isNull()
        assertThat(findAnotherMethod?.default).isTrue()
    }

    @DisplayName("결제 수단 제거 예외")
    @Test
    fun removeMethodException() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val defaultMethod = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val vendingMachine = machineRepository.save(Machine(BaseServiceTest.MACHINE_NO, "Test machine", MachineType.VENDING, Address(), 100))
        val cup = cupRepository.save(Cup(BaseServiceTest.CUP_RFID))
        val rental = Rental(user, vendingMachine, true, 10)
        rental.setRentalCup(cup)
        rentalRepository.save(rental)

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            delete("$endPoint/method/${defaultMethod.id}")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("code", `is`(ErrorCode.PAYMENT_METHOD_REMOVE_NOT_ALLOWED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.PAYMENT_METHOD_REMOVE_NOT_ALLOWED.message)))
    }

    @DisplayName("결제 수단 제거 예외 TC2")
    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @Test
    fun removeMethodExceptionTc2() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))

        //when, then
        mockMvc.perform(
            delete("$endPoint/method/${method.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
            .andExpect(jsonPath("code", `is`(ErrorCode.ACCESS_DENIED.code)))
            .andExpect(jsonPath("message", `is`(ErrorCode.ACCESS_DENIED.message)))
    }

    @DisplayName("결제 카테고리 목록 조회")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun getAllCategories() {
        insertCategories()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectAmountsValue = listOf(10, 20, 40)
        val expectPriceValue = listOf(1000, 2000, 4000)
        val expectDiscountRateValue = listOf(10, 0, 10)
        val expectDiscountPriceValue = listOf(900, 2000, 3600)

        mockMvc.perform(
            get("${endPoint}/category")
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("pageable.totalElements", `is`(5)))
            .andExpect(jsonPath("content[*].amounts", `is`(expectAmountsValue)))
            .andExpect(jsonPath("content[*].price", `is`(expectPriceValue)))
            .andExpect(jsonPath("content[*].discountRate", `is`(expectDiscountRateValue)))
            .andExpect(jsonPath("content[*].discountPrice", `is`(expectDiscountPriceValue)))
    }

    @DisplayName("결제 카테고리 목록 조회 TC2")
    @Test
    fun getAllCategoriesTc2() {
        //given
        val group = groupRepository.save(Group("Group", GroupType.UNIVERSITY, 20))
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.updateGroup(group)
        userRepository.save(user)
        insertCategories()

        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectAmountsValue = listOf(10, 20, 40)
        val expectPriceValue = listOf(1000, 2000, 4000)
        val expectDiscountRateValue = listOf(10, 0, 10)
        val expectDiscountPriceValue = listOf(720, 1600, 2880)

        mockMvc.perform(
            get("${endPoint}/category")
                .cookie(cookie)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("pageable.totalElements", `is`(5)))
            .andExpect(jsonPath("content[*].amounts", `is`(expectAmountsValue)))
            .andExpect(jsonPath("content[*].price", `is`(expectPriceValue)))
            .andExpect(jsonPath("content[*].discountRate", `is`(expectDiscountRateValue)))
            .andExpect(jsonPath("content[*].discountPrice", `is`(expectDiscountPriceValue)))
    }

    private fun insertCategories() {
        val before10Days = Instant.now().minus(10, ChronoUnit.DAYS)
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(30, 3000, 10, after10Days, before10Days))
        paymentCategoryRepository.save(PaymentCategory(40, 4000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(60, 6000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(20, 2000, 10, before10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(50, 5000, 10, after10Days, after10Days))
    }
}