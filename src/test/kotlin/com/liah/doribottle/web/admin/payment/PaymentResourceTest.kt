package com.liah.doribottle.web.admin.payment

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.payment.*
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType
import com.liah.doribottle.domain.payment.card.CardProvider
import com.liah.doribottle.domain.payment.card.CardType
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.payment.vm.PaymentCategoryRegisterOrUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/payment"

    @Autowired
    private lateinit var paymentRepository: PaymentRepository
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var pointRepository: PointRepository

    @AfterEach
    internal fun destroy() {
        paymentRepository.deleteAll()
        pointRepository.deleteAll()
        paymentCategoryRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("유저 결제 내역 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        val userA = userRepository.save(User("010-1111-1111", "A", "010-1111-1111", Role.USER))
        val userB = userRepository.save(User("010-2222-2222", "B", "010-2222-2222", Role.USER))
        val userC = userRepository.save(User("010-3333-3333", "C", "010-3333-3333", Role.USER))
        insertPayments(userA, userB, userC)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "4")
        params.add("type", PaymentType.SAVE_POINT.name)

        val expectUserId = listOf(userC.id.toString(), userB.id.toString(), userB.id.toString(), userA.id.toString())
        val expectPrice = listOf(6000, 4000, 3000, 1000)
        val expectType = listOf(PaymentType.SAVE_POINT.name, PaymentType.SAVE_POINT.name, PaymentType.SAVE_POINT.name, PaymentType.SAVE_POINT.name)
        val expectStatus = listOf(PaymentStatus.CANCELED.name, PaymentStatus.SUCCEEDED.name, PaymentStatus.CANCELED.name, PaymentStatus.SUCCEEDED.name)

        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].user.id", `is`(expectUserId)))
            .andExpect(jsonPath("content[*].price", `is`(expectPrice)))
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].status", `is`(expectStatus)))
    }

    private fun insertPayments(userA: User, userB: User, userC: User) {
        val card = Card(CardProvider.HYUNDAI, CardProvider.HYUNDAI, "1234", CardType.CREDIT, CardOwnerType.PERSONAL)
        val payment1 = Payment(userA, 1000, PaymentType.SAVE_POINT, card)
        val point1 = pointRepository.save(Point(userA.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))
        val result1 = PaymentResult("dummyPaymentKey1", Instant.now(), null, null)
        payment1.updateResult(result1, point1)
        paymentRepository.save(payment1)

        val payment2 = Payment(userA, 2000, PaymentType.LOST_CUP, card)
        val result2 = PaymentResult("dummyPaymentKey2", Instant.now(), null, null)
        payment2.updateResult(result2, null)
        paymentRepository.save(payment2)

        val payment3 = Payment(userB, 3000, PaymentType.SAVE_POINT, card)
        val point3 = pointRepository.save(Point(userB.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 30))
        val result3 = PaymentResult("dummyPaymentKey3", Instant.now(), null, "dummyCancelKey3")
        payment3.updateResult(result3, point3)
        paymentRepository.save(payment3)

        val payment4 = Payment(userB, 4000, PaymentType.SAVE_POINT, card)
        val point4 = pointRepository.save(Point(userB.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 40))
        val result4 = PaymentResult("dummyPaymentKey4", Instant.now(), null, null)
        payment4.updateResult(result4, point4)
        paymentRepository.save(payment4)

        val payment5 = Payment(userC, 5000, PaymentType.LOST_CUP, card)
        val result5 = PaymentResult("dummyPaymentKey5", Instant.now(), null, null)
        payment5.updateResult(result5, null)
        paymentRepository.save(payment5)

        val payment6 = Payment(userC, 6000, PaymentType.SAVE_POINT, card)
        val point6 = pointRepository.save(Point(userC.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 60))
        val result6 = PaymentResult("dummyPaymentKey6", Instant.now(), null, "dummyCancelKey6")
        payment6.updateResult(result6, point6)
        paymentRepository.save(payment6)
    }

    @DisplayName("유저 결제 내역 단건 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val user = userRepository.save(User("010-1111-1111", "A", "010-1111-1111", Role.USER))
        val card = Card(CardProvider.HYUNDAI, CardProvider.HYUNDAI, "1234", CardType.CREDIT, CardOwnerType.PERSONAL)
        val payment = Payment(user, 1000, PaymentType.SAVE_POINT, card)
        val point = pointRepository.save(Point(user.id, PointSaveType.PAY, PointEventType.SAVE_PAY, 10))
        val result = PaymentResult("dummyPaymentKey1", Instant.now(), null, null)
        payment.updateResult(result, point)
        paymentRepository.save(payment)

//        mockMvc.perform(
//            get("${endPoint}/${payment.id}")
//                .contentType(MediaType.APPLICATION_JSON)
//                .accept(MediaType.APPLICATION_JSON)
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("content[*].userId", `is`(expectUserId)))
//            .andExpect(jsonPath("content[*].price", `is`(expectPrice)))
//            .andExpect(jsonPath("content[*].type", `is`(expectType)))
//            .andExpect(jsonPath("content[*].status", `is`(expectStatus)))
    }

    @DisplayName("결제 카테고리 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun registerCategory() {
        val body = PaymentCategoryRegisterOrUpdateRequest(10, 1000, 10, null, null)

        mockMvc.perform(
            post("${endPoint}/category")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("결제 카테고리 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAllCategories() {
        insertCategories()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectAmountsValue = listOf(60, 50, 40)
        val expectPriceValue = listOf(6000, 5000, 4000)
        val expectDiscountRateValue = listOf(10, 10, 10)
        val expectDiscountPriceValue = listOf(5400, 4500, 3600)

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

    @DisplayName("결제 카테고리 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getCategory() {
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, null, null))

        mockMvc.perform(
            get("${endPoint}/category/${category.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("amounts", `is`(category.amounts.toInt())))
            .andExpect(jsonPath("price", `is`(category.price.toInt())))
            .andExpect(jsonPath("discountRate", `is`(category.discountRate)))
            .andExpect(jsonPath("discountPrice", `is`(900)))
    }

    @DisplayName("결제 카테고리 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun updateCategory() {
        //given
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, null, null))
        val body = PaymentCategoryRegisterOrUpdateRequest(20, 2000, 20, null, null)

        //when, then
        mockMvc.perform(
            put("${endPoint}/category/${category.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)
        assertThat(findCategory?.amounts).isEqualTo(20)
        assertThat(findCategory?.price).isEqualTo(2000)
        assertThat(findCategory?.discountRate).isEqualTo(20)
    }

    @DisplayName("결제 카테고리 제거")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun removeCategory() {
        val category = paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, null, null))

        mockMvc.perform(
            delete("${endPoint}/category/${category.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)
        assertThat(findCategory).isNull()
    }

    private fun insertCategories() {
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        paymentCategoryRepository.save(PaymentCategory(10, 1000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(20, 2000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(30, 3000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(40, 4000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(50, 5000, 10, after10Days, after10Days))
        paymentCategoryRepository.save(PaymentCategory(60, 6000, 10, after10Days, after10Days))
    }
}