package com.liah.doribottle.service.payment

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
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.CardDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit

class PaymentServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var paymentService: PaymentService
    @Autowired
    private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("결제 수단 등록")
    @Test
    fun registerMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val billingInfo = BillingInfo("dummyKey", TOSS_PAYMENTS, CARD, CardDto(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), Instant.now())
        clear()

        //when
        val id = paymentService.registerMethod(user.id, billingInfo)
        clear()

        //then
        val findMethod = paymentMethodRepository.findByIdOrNull(id)

        assertThat(findMethod?.user).isEqualTo(user)
        assertThat(findMethod?.billingKey).isEqualTo("dummyKey")
        assertThat(findMethod?.providerType).isEqualTo(TOSS_PAYMENTS)
        assertThat(findMethod?.type).isEqualTo(CARD)
        assertThat(findMethod?.default).isEqualTo(true)
        assertThat(findMethod?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findMethod?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findMethod?.card?.number).isEqualTo("1234")
        assertThat(findMethod?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findMethod?.card?.cardOwnerType).isEqualTo(PERSONAL)
    }

    @DisplayName("결제 수단 등록 TC2")
    @Test
    fun registerMethodTc2() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val billingInfo = BillingInfo("dummyKey2", TOSS_PAYMENTS, CARD, CardDto(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), Instant.now())
        clear()

        //when
        val id = paymentService.registerMethod(user.id, billingInfo)
        clear()

        //then
        val findMethod = paymentMethodRepository.findByIdOrNull(id)

        assertThat(findMethod?.user).isEqualTo(user)
        assertThat(findMethod?.billingKey).isEqualTo("dummyKey2")
        assertThat(findMethod?.providerType).isEqualTo(TOSS_PAYMENTS)
        assertThat(findMethod?.type).isEqualTo(CARD)
        assertThat(findMethod?.default).isEqualTo(false)
        assertThat(findMethod?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findMethod?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findMethod?.card?.number).isEqualTo("1234")
        assertThat(findMethod?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findMethod?.card?.cardOwnerType).isEqualTo(PERSONAL)
    }

    @DisplayName("결제 수단 목록 조회")
    @Test
    fun getAllMethods() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        insertMethods(user)
        clear()

        //when
        val result = paymentService.getAllMethods(user.id, Pageable.ofSize(3))

        //then
        assertThat(result)
            .extracting("billingKey")
            .containsExactly("dummyKey1", "dummyKey2", "dummyKey3")
        assertThat(result)
            .extracting("card.issuerProvider")
            .containsExactly(KOOKMIN, HYUNDAI, SAMSUNG)
        assertThat(result)
            .extracting("default")
            .containsExactly(false, true, false)
    }

    private fun insertMethods(user: User) {
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "1", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey2", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "2", CREDIT, PERSONAL), true, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey3", TOSS_PAYMENTS, CARD, Card(SAMSUNG, SAMSUNG, "3", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey4", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4", CREDIT, CORPORATE), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey5", TOSS_PAYMENTS, CARD, Card(BC, BC, "5", CREDIT, PERSONAL), false, Instant.now()))
        paymentMethodRepository.save(PaymentMethod(user,"dummyKey6", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "6", CREDIT, PERSONAL), false, Instant.now()))
    }

    @DisplayName("결제 카테고리 등록")
    @Test
    fun registerCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)

        //when
        val id = paymentService.registerCategory(10, 1000, 10, after10Days, after10Days)
        clear()

        //then
        val findCategory = paymentCategoryRepository.findByIdOrNull(id)

        assertThat(findCategory?.amounts).isEqualTo(10)
        assertThat(findCategory?.price).isEqualTo(1000)
        assertThat(findCategory?.discountRate).isEqualTo(10)
        assertThat(findCategory?.discountExpiredDate).isEqualTo(after10Days)
        assertThat(findCategory?.expiredDate).isEqualTo(after10Days)
    }

    @DisplayName("결제 카테고리 상세 조회")
    @Test
    fun getCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(
            PaymentCategory(10, 1000, 10, after10Days, after10Days)
        )
        clear()

        //when
        val categoryDto = paymentService.getCategory(category.id)

        //then
        assertThat(categoryDto.amounts).isEqualTo(10)
        assertThat(categoryDto.price).isEqualTo(1000)
        assertThat(categoryDto.discountRate).isEqualTo(10)
        assertThat(categoryDto.discountExpiredDate).isEqualTo(after10Days)
        assertThat(categoryDto.expiredDate).isEqualTo(after10Days)
    }

    @DisplayName("결제 카테고리 목록 조회")
    @Test
    fun getAllCategories() {
        //given
        insertCategories()
        clear()

        //when
        val result = paymentService.getAllCategories(
            expired = false,
            deleted = false,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("amounts")
            .containsExactly(10L, 20L, 30L)
        assertThat(result)
            .extracting("price")
            .containsExactly(1000L, 2000L, 3000L)
    }

    @DisplayName("결제 카테고리 상세 조회")
    @Test
    fun removeCategory() {
        //given
        val after10Days = Instant.now().plus(10, ChronoUnit.DAYS)
        val category = paymentCategoryRepository.save(
            PaymentCategory(10, 1000, 10, after10Days, after10Days)
        )
        clear()

        //when
        paymentService.removeCategory(category.id)
        clear()

        //then
        val findCategory = paymentCategoryRepository.findByIdOrNull(category.id)

        assertThat(findCategory?.deleted).isEqualTo(true)
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