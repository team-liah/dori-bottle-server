package com.liah.doribottle.service.payment

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.payment.Payment
import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.TOSS_PAYMENTS
import com.liah.doribottle.domain.payment.PaymentMethodType.CARD
import com.liah.doribottle.domain.payment.PaymentStatus.*
import com.liah.doribottle.domain.payment.PaymentType.LOST_CUP
import com.liah.doribottle.domain.payment.PaymentType.SAVE_POINT
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType.CORPORATE
import com.liah.doribottle.domain.payment.card.CardOwnerType.PERSONAL
import com.liah.doribottle.domain.payment.card.CardProvider.*
import com.liah.doribottle.domain.payment.card.CardType.CREDIT
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.payment.PaymentCategoryRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.payment.PaymentRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.payment.dto.BillingInfo
import com.liah.doribottle.service.payment.dto.CardDto
import com.liah.doribottle.service.payment.dto.PaymentResultDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class PaymentServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var paymentService: PaymentService
    @Autowired
    private lateinit var paymentRepository: PaymentRepository
    @Autowired
    private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired
    private lateinit var paymentCategoryRepository: PaymentCategoryRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @DisplayName("결제 생성")
    @Test
    fun create() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = CardDto(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        clear()

        //when
        val id = paymentService.create(user.id, 1000, SAVE_POINT, card)
        clear()

        //then
        val findPayment = paymentRepository.findByIdOrNull(id)
        assertThat(findPayment?.user).isEqualTo(user)
        assertThat(findPayment?.price).isEqualTo(1000)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.number).isEqualTo("1234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(PROCEEDING)
        assertThat(findPayment?.result).isNull()
        assertThat(findPayment?.pointId).isNull()
    }

    @DisplayName("결제 결과 업데이트")
    @Test
    fun updateResult() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val payment = paymentRepository.save(Payment(user, 3000, SAVE_POINT, card))
        val result = PaymentResultDto("dummyPaymentKey", Instant.now(), "")
        val pointId = UUID.randomUUID()
        clear()

        //when
        paymentService.updateResult(payment.id, result, pointId)
        clear()

        //then
        val findPayment = paymentRepository.findByIdOrNull(payment.id)
        assertThat(findPayment?.user).isEqualTo(user)
        assertThat(findPayment?.price).isEqualTo(3000)
        assertThat(findPayment?.type).isEqualTo(SAVE_POINT)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.number).isEqualTo("1234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(SUCCEEDED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo("dummyPaymentKey")
        assertThat(findPayment?.pointId).isEqualTo(pointId)
    }

    @DisplayName("결제 결과 업데이트 TC2")
    @Test
    fun updateResultTc2() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val payment = paymentRepository.save(Payment(user, 5000, LOST_CUP, card))
        val result = PaymentResultDto("dummyPaymentKey", Instant.now(), "")
        clear()

        //when
        paymentService.updateResult(payment.id, result, null)
        clear()

        //then
        val findPayment = paymentRepository.findByIdOrNull(payment.id)
        assertThat(findPayment?.user).isEqualTo(user)
        assertThat(findPayment?.price).isEqualTo(5000)
        assertThat(findPayment?.type).isEqualTo(LOST_CUP)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.number).isEqualTo("1234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(SUCCEEDED)
        assertThat(findPayment?.result?.paymentKey).isEqualTo("dummyPaymentKey")
        assertThat(findPayment?.pointId).isNull()
    }

    @DisplayName("결제 결과 업데이트 TC3")
    @Test
    fun updateResultTc3() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val payment = paymentRepository.save(Payment(user, 5000, LOST_CUP, card))
        clear()

        //when
        paymentService.updateResult(payment.id, null, null)
        clear()

        //then
        val findPayment = paymentRepository.findByIdOrNull(payment.id)
        assertThat(findPayment?.user).isEqualTo(user)
        assertThat(findPayment?.price).isEqualTo(5000)
        assertThat(findPayment?.type).isEqualTo(LOST_CUP)
        assertThat(findPayment?.card?.issuerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.acquirerProvider).isEqualTo(HYUNDAI)
        assertThat(findPayment?.card?.number).isEqualTo("1234")
        assertThat(findPayment?.card?.cardType).isEqualTo(CREDIT)
        assertThat(findPayment?.card?.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(findPayment?.status).isEqualTo(FAILED)
        assertThat(findPayment?.result).isNull()
        assertThat(findPayment?.pointId).isNull()
    }

    @DisplayName("결제 결과 업데이트 예외")
    @Test
    fun updateResultException() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val card = Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL)
        val paymentToSavePoint = paymentRepository.save(Payment(user, 5000, SAVE_POINT, card))
        val paymentToLostCup = paymentRepository.save(Payment(user, 5000, LOST_CUP, card))
        clear()

        //when, then
        val exception1 = assertThrows<IllegalArgumentException> {
            val result = PaymentResultDto("dummyPaymentKey", Instant.now(), "")
            paymentService.updateResult(paymentToSavePoint.id, result, null)
        }
        assertThat(exception1.message).isEqualTo("Null pointId is not allowed if payment type is SAVE_POINT")

        val exception2 = assertThrows<IllegalArgumentException> {
            val result = PaymentResultDto("dummyPaymentKey", Instant.now(), "")
            paymentService.updateResult(paymentToLostCup.id, result, UUID.randomUUID())
        }
        assertThat(exception2.message).isEqualTo("PointId is not allowed if payment type is not SAVE_POINT")
    }

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

    @DisplayName("결제 수단 조회")
    @Test
    fun getMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        clear()

        //when
        val result = paymentService.getMethod(method.id)
        clear()

        //then
        assertThat(result.userId).isEqualTo(user.id)
        assertThat(result.billingKey).isEqualTo("dummyKey")
        assertThat(result.providerType).isEqualTo(TOSS_PAYMENTS)
        assertThat(result.type).isEqualTo(CARD)
        assertThat(result.card.issuerProvider).isEqualTo(KOOKMIN)
        assertThat(result.card.acquirerProvider).isEqualTo(KOOKMIN)
        assertThat(result.card.number).isEqualTo("4321")
        assertThat(result.card.cardType).isEqualTo(CREDIT)
        assertThat(result.card.cardOwnerType).isEqualTo(PERSONAL)
        assertThat(result.default).isEqualTo(true)
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

    @DisplayName("기본 결제 수단 변경")
    @Test
    fun changeDefaultMethod() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val method1 = paymentMethodRepository.save(PaymentMethod(user,"dummyKey1", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        val method2 = paymentMethodRepository.save(PaymentMethod(user,"dummyKey2", TOSS_PAYMENTS, CARD, Card(HYUNDAI, HYUNDAI, "1234", CREDIT, PERSONAL), false, Instant.now()))
        clear()

        //when
        paymentService.changeDefaultMethod(method2.id, user.id)
        clear()

        //then
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
        clear()

        //when
        paymentService.removeMethod(method.id)
        clear()

        //then
        val findMethod = paymentMethodRepository.findByIdOrNull(method.id)
        assertThat(findMethod).isNull()
    }

    @DisplayName("결제 수단 제거 예외")
    @Test
    fun removeMethodException() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        val defaultMethod = paymentMethodRepository.save(PaymentMethod(user,"dummyKey", TOSS_PAYMENTS, CARD, Card(KOOKMIN, KOOKMIN, "4321", CREDIT, PERSONAL), true, Instant.now()))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            paymentService.removeMethod(defaultMethod.id)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.PAYMENT_METHOD_REMOVE_NOT_ALLOWED)
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