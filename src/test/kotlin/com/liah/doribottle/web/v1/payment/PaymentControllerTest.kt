package com.liah.doribottle.web.v1.payment

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.TOSS_PAYMENTS
import com.liah.doribottle.domain.payment.PaymentMethodType.CARD
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType.CORPORATE
import com.liah.doribottle.domain.payment.card.CardOwnerType.PERSONAL
import com.liah.doribottle.domain.payment.card.CardProvider.*
import com.liah.doribottle.domain.payment.card.CardType.CREDIT
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.CardDto
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.payment.vm.PaymentMethodRegisterRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
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

class PaymentControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/payment"

    @Autowired
    private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var mockTossPaymentsService: TossPaymentsService

    @AfterEach
    internal fun destroy() {
        paymentMethodRepository.deleteAll()
        paymentCategoryRepository.deleteAll()
        userRepository.deleteAll()
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
                .content(body.convertJsonToString())
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

    @DisplayName("결제 수단 제거 예외")
    @Test
    fun removeMethodException() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        //when, then
        mockMvc.perform(
            delete("$endPoint/method/${method.id}")
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

        val expectAmountsValue = listOf(10, 20, 30)
        val expectPriceValue = listOf(1000, 2000, 3000)
        val expectDiscountRateValue = listOf(10, 10, 10)
        val expectDiscountPriceValue = listOf(900, 1800, 2700)

        mockMvc.perform(
            get("${endPoint}/category")
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].amounts", `is`(expectAmountsValue)))
            .andExpect(jsonPath("content[*].price", `is`(expectPriceValue)))
            .andExpect(jsonPath("content[*].discountRate", `is`(expectDiscountRateValue)))
            .andExpect(jsonPath("content[*].discountPrice", `is`(expectDiscountPriceValue)))
    }

    private fun insertCategories() {
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(30, 3000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(40, 4000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(60, 6000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(20, 2000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(50, 5000, 10, after10Days, after10Days))
    }
}