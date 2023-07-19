package com.liah.doribottle.service.rental

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.SAVE_PAY
import com.liah.doribottle.domain.point.PointEventType.USE_CUP
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.point.PointEventRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@SpringBootTest
@Transactional
class RentalServiceTest {
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    @Autowired private lateinit var rentalService: RentalService
    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var pointEventRepository: PointEventRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var cupRepository: CupRepository
    @Autowired private lateinit var machineRepository: MachineRepository

    companion object {
        private const val USER_LOGIN_ID = "010-5638-3316"
        private const val CUP_RFID = "A1:A1:A1:A1"
        private const val MACHINE_NO1 = "000-00001"
        private const val MACHINE_NO2 = "000-00002"
    }

    private lateinit var user: User
    private lateinit var cup: Cup
    private lateinit var vendingMachine: Machine
    private lateinit var collectionMachine: Machine

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)

        cup = cupRepository.save(Cup(CUP_RFID))
        cup.changeState(CupStatus.AVAILABLE)

        vendingMachine = machineRepository.save(Machine(MACHINE_NO1, VENDING, Address(),100))
        vendingMachine.increaseCupAmounts(10)

        collectionMachine = machineRepository.save(Machine(MACHINE_NO2, COLLECTION, Address(),100))
        collectionMachine.increaseCupAmounts(0)
    }

    @DisplayName("얼음 컵 대여")
    @Test
    fun rentIceCup() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, true)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoint = pointRepository.findByUserId(user.id)
        val findPointEvents = pointEventRepository.findAllByPointId(findPoint?.id!!)
        val findMachine = machineRepository.findByIdOrNull(vendingMachine.id)

        assertThat(findRental?.user).isEqualTo(user)
        assertThat(findRental?.cup).isEqualTo(cup)
        assertThat(findRental?.fromMachine).isEqualTo(vendingMachine)
        assertThat(findRental?.toMachine).isNull()
        assertThat(findRental?.withIce).isEqualTo(true)
        assertThat(findRental?.cost).isEqualTo(2L)
        assertThat(findRental?.succeededDate).isNull()
        assertThat(findRental?.expiredDate).isAfter(Instant.now())
        assertThat(findRental?.status).isEqualTo(PROCEEDING)

        assertThat(findPoint.remainAmounts).isEqualTo(8L)
        assertThat(findPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findPointEvents)
            .extracting("amounts")
            .containsExactly(10L, 2L)

        assertThat(findMachine?.cupAmounts).isEqualTo(9)
    }

    @DisplayName("컵 대여")
    @Test
    fun rentCup() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, false)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoint = pointRepository.findByUserId(user.id)
        val findPointEvents = pointEventRepository.findAllByPointId(findPoint?.id!!)
        val findMachine = machineRepository.findByIdOrNull(vendingMachine.id)

        assertThat(findRental?.user).isEqualTo(user)
        assertThat(findRental?.cup).isEqualTo(cup)
        assertThat(findRental?.fromMachine).isEqualTo(vendingMachine)
        assertThat(findRental?.toMachine).isNull()
        assertThat(findRental?.withIce).isEqualTo(false)
        assertThat(findRental?.cost).isEqualTo(1L)
        assertThat(findRental?.succeededDate).isNull()
        assertThat(findRental?.expiredDate).isAfter(Instant.now())
        assertThat(findRental?.status).isEqualTo(PROCEEDING)

        assertThat(findPoint.remainAmounts).isEqualTo(9L)
        assertThat(findPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findPointEvents)
            .extracting("amounts")
            .containsExactly(10L, 1L)

        assertThat(findMachine?.cupAmounts).isEqualTo(9)
    }

    @DisplayName("컵 대여 예외")
    @Test
    fun rentalException() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))
        clear()

        //when, then
        val exception1 = assertThrows<BusinessException> {
            rentalService.rent(user.id, CUP_RFID, vendingMachine.id, true)
        }
        assertThat(exception1.errorCode).isEqualTo(ErrorCode.LACK_OF_POINT)

        val exception2 = assertThrows<BusinessException> {
            rentalService.rent(user.id, "00000000", vendingMachine.id, true)
        }
        assertThat(exception2.errorCode).isEqualTo(ErrorCode.CUP_NOT_FOUND)
    }

    @DisplayName("컵 반납")
    @Test
    fun `return`() {
        //given
        val rental = rentalRepository.save(Rental(user, cup, vendingMachine, true, 7))
        clear()

        //when
        rentalService.`return`(collectionMachine.id, cup.rfid)

        //then
        val findRental = rentalRepository.findByIdOrNull(rental.id)
        val findCup = cupRepository.findByIdOrNull(cup.id)
        val fromMachine = machineRepository.findByIdOrNull(collectionMachine.id)

        assertThat(findRental?.toMachine).isEqualTo(fromMachine)
        assertThat(findRental?.status).isEqualTo(RentalStatus.SUCCEEDED)
        assertThat(findRental?.succeededDate).isNotNull

        assertThat(findCup?.status).isEqualTo(CupStatus.RETURNED)

        assertThat(fromMachine?.cupAmounts).isEqualTo(1)
    }

    @DisplayName("컵 반납 예외")
    @Test
    fun returnException() {
        //given
        rentalRepository.save(Rental(user, cup, vendingMachine, true, 7))
        cupRepository.save(Cup("B1:B1:B1:B1"))
        clear()

        //when, then
        val exception1 = assertThrows<BusinessException> {
            rentalService.`return`(collectionMachine.id, "000000000")
        }
        assertThat(exception1.errorCode).isEqualTo(ErrorCode.CUP_NOT_FOUND)

        val exception2 = assertThrows<BusinessException> {
            rentalService.`return`(collectionMachine.id, "B1:B1:B1:B1")
        }
        assertThat(exception2.errorCode).isEqualTo(ErrorCode.RENTAL_NOT_FOUND)
    }
}