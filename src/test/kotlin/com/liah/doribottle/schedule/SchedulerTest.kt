package com.liah.doribottle.schedule

import com.liah.doribottle.common.error.exception.BillingExecuteException
import com.liah.doribottle.constant.LOST_CUP_PRICE
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus.AVAILABLE
import com.liah.doribottle.domain.cup.CupStatus.LOST
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.PaymentMethodProviderType.TOSS_PAYMENTS
import com.liah.doribottle.domain.payment.PaymentMethodType.CARD
import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType.LOST_CUP
import com.liah.doribottle.domain.payment.card.Card
import com.liah.doribottle.domain.payment.card.CardOwnerType.PERSONAL
import com.liah.doribottle.domain.payment.card.CardProvider.KOOKMIN
import com.liah.doribottle.domain.payment.card.CardType.CREDIT
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.payment.PaymentRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.payment.dto.PaymentResultDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant
import java.util.*

@SpringBootTest
class SchedulerTest {
    @Autowired private lateinit var scheduler: Scheduler
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var paymentRepository: PaymentRepository
    @Autowired private lateinit var paymentMethodRepository: PaymentMethodRepository
    @Autowired private lateinit var machineRepository: MachineRepository
    @Autowired private lateinit var cupRepository: CupRepository

    @MockBean
    private lateinit var mockTossPaymentsService: TossPaymentsService

    @AfterEach
    internal fun destroy() {
        paymentRepository.deleteAll()
        paymentMethodRepository.deleteAll()
        rentalRepository.deleteAll()
        machineRepository.deleteAll()
        cupRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("대여 컵 분실 처리(결제, 상태 변경)")
    @Test
    fun payToLostCupTask() {
        //given
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val cup1 = cupRepository.save(Cup("00 00 00 00"))
        val cup2 = cupRepository.save(Cup("11 11 11 11"))

        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val billingKey = "dummyBillingKey"
        val paymentKey = "dummyPaymentKey"
        val card = Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, billingKey, TOSS_PAYMENTS, CARD, card, true, Instant.now()))
        val rental1 = Rental(user, machine, true, 0)
        rental1.setRentalCup(cup1)
        rentalRepository.save(rental1)
        val rental2 = Rental(user, machine, true, 0)
        rental2.setRentalCup(cup2)
        rentalRepository.save(rental2)

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP)))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, null))

        //when
        scheduler.payToLostCupTask()

        //then
        verify(mockTossPaymentsService, times(2))
            .executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP))

        val findPayments = paymentRepository.findAll()
        val findRental1 = rentalRepository.findByIdOrNull(rental1.id)
        val findRental2 = rentalRepository.findByIdOrNull(rental2.id)
        val findCup1 = cupRepository.findByIdOrNull(cup1.id)
        val findCup2 = cupRepository.findByIdOrNull(cup2.id)

        assertThat(findPayments)
            .extracting("price")
            .containsExactly(LOST_CUP_PRICE, LOST_CUP_PRICE)
        assertThat(findPayments)
            .extracting("type")
            .containsExactly(LOST_CUP, LOST_CUP)
        assertThat(findPayments)
            .extracting("status")
            .containsExactly(PaymentStatus.SUCCEEDED, PaymentStatus.SUCCEEDED)
        assertThat(findPayments)
            .extracting("card")
            .containsExactly(card, card)

        assertThat(findRental1?.status).isEqualTo(RentalStatus.FAILED)
        assertThat(findRental2?.status).isEqualTo(RentalStatus.FAILED)

        assertThat(findCup1?.status).isEqualTo(LOST)
        assertThat(findCup2?.status).isEqualTo(LOST)
    }

    @DisplayName("대여 컵 분실 처리(결제, 상태 변경) - rentalService.fail 예외")
    @Test
    fun payToLostCupTaskFailException() {
        //given
        val vendingMachine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val collectionMachine = machineRepository.save(Machine("0000002", "name2", COLLECTION, Address("00001", "삼성로", null), 100))
        val cup1 = cupRepository.save(Cup("00 00 00 00"))
        val cup2 = cupRepository.save(Cup("11 11 11 11"))

        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val billingKey = "dummyBillingKey"
        val paymentKey = "dummyPaymentKey"
        val card = Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, billingKey, TOSS_PAYMENTS, CARD, card, true, Instant.now()))
        val rental1 = Rental(user, vendingMachine, true, 0)
        rental1.setRentalCup(cup1)
        rental1.`return`(collectionMachine)
        rentalRepository.save(rental1)
        val rental2 = Rental(user, vendingMachine, true, 0)
        rental2.setRentalCup(cup2)
        rentalRepository.save(rental2)

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP)))
            .willReturn(PaymentResultDto(paymentKey, Instant.now(), null, null))

        //when
        scheduler.payToLostCupTask()

        //then
        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP))

        val findPayments = paymentRepository.findAll()
        val findRental1 = rentalRepository.findByIdOrNull(rental1.id)
        val findRental2 = rentalRepository.findByIdOrNull(rental2.id)
        val findCup1 = cupRepository.findByIdOrNull(cup1.id)
        val findCup2 = cupRepository.findByIdOrNull(cup2.id)

        assertThat(findPayments)
            .extracting("price")
            .containsExactly(LOST_CUP_PRICE)
        assertThat(findPayments)
            .extracting("type")
            .containsExactly(LOST_CUP)
        assertThat(findPayments)
            .extracting("status")
            .containsExactly(PaymentStatus.SUCCEEDED)
        assertThat(findPayments)
            .extracting("card")
            .containsExactly(card)

        assertThat(findRental1?.status).isEqualTo(RentalStatus.SUCCEEDED)
        assertThat(findRental2?.status).isEqualTo(RentalStatus.FAILED)

        assertThat(findCup1?.status).isEqualTo(AVAILABLE)
        assertThat(findCup2?.status).isEqualTo(LOST)
    }

    @DisplayName("대여 컵 분실 처리(결제, 상태 변경) - tossPaymentsService.executeBilling 예외")
    @Test
    fun payToLostCupTaskBillingException() {
        //given
        val machine = machineRepository.save(Machine("0000001", "name", VENDING, Address("00001", "삼성로", null), 100))
        val cup = cupRepository.save(Cup("00 00 00 00"))

        val user = userRepository.save(User("010-0000-0000", "Tester", "010-0000-0000", Role.USER))
        val billingKey = "dummyBillingKey"
        val card = Card(KOOKMIN, KOOKMIN, "12341234", CREDIT, PERSONAL)
        paymentMethodRepository.save(PaymentMethod(user, billingKey, TOSS_PAYMENTS, CARD, card, true, Instant.now()))
        val rental = Rental(user, machine, true, 0)
        rental.setRentalCup(cup)
        rentalRepository.save(rental)

        given(mockTossPaymentsService.executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP)))
            .willThrow(BillingExecuteException())

        //when
        scheduler.payToLostCupTask()

        //then
        verify(mockTossPaymentsService, times(1))
            .executeBilling(eq(billingKey), eq(user.id), eq(LOST_CUP_PRICE), any<UUID>(), eq(LOST_CUP))

        val findPayment = paymentRepository.findAll().firstOrNull()
        val findRental = rentalRepository.findByIdOrNull(rental.id)
        val findCup = cupRepository.findByIdOrNull(cup.id)

        assertThat(findPayment?.price).isEqualTo(LOST_CUP_PRICE)
        assertThat(findPayment?.type).isEqualTo(LOST_CUP)
        assertThat(findPayment?.status).isEqualTo(PaymentStatus.FAILED)
        assertThat(findPayment?.card).isEqualTo(card)

        assertThat(findRental?.status).isEqualTo(RentalStatus.FAILED)

        assertThat(findCup?.status).isEqualTo(LOST)
    }
}