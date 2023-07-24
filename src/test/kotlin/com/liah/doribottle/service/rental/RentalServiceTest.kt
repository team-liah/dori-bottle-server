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
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.point.PointEventRepository
import com.liah.doribottle.repository.point.PointHistoryRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.time.Instant

class RentalServiceTest : BaseServiceTest() {
    @Autowired private lateinit var rentalService: RentalService
    @Autowired private lateinit var rentalRepository: RentalRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var pointEventRepository: PointEventRepository
    @Autowired private lateinit var pointHistoryRepository: PointHistoryRepository
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var cupRepository: CupRepository
    @Autowired private lateinit var machineRepository: MachineRepository

    private lateinit var user: User
    private lateinit var cup: Cup
    private lateinit var vendingMachine: Machine
    private lateinit var collectionMachine: Machine

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)

        cup = cupRepository.save(Cup(CUP_RFID))

        vendingMachine = machineRepository.save(Machine(MACHINE_NO1, MACHINE_NAME, VENDING, Address("12345", "test"),100))
        vendingMachine.increaseCupAmounts(10)

        collectionMachine = machineRepository.save(Machine(MACHINE_NO2, MACHINE_NAME, COLLECTION, Address("12345", "test"),100))
        collectionMachine.increaseCupAmounts(0)
    }

    @DisplayName("얼음 컵 대여")
    @Test
    fun rentIceCup() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, true)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoint = pointRepository.findAllByUserId(user.id).first()
        val findPointEvents = pointEventRepository.findAllByPointId(findPoint.id)
        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)
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
            .containsExactly(10L, -2L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(10L, -2L)

        assertThat(findMachine?.cupAmounts).isEqualTo(9)
    }

    @DisplayName("얼음 컵 대여 TC2")
    @Test
    fun rentIceCupTc2() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 1))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 1))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, true)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoints = pointRepository.findAllByUserId(user.id)
        val firstPoint = findPoints.first()
        val secondPoint = findPoints.last()
        val findFirstPointEvents = pointEventRepository.findAllByPointId(firstPoint.id)
        val findSecondPointEvents = pointEventRepository.findAllByPointId(secondPoint.id)
        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)
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

        assertThat(firstPoint.remainAmounts).isEqualTo(0L)
        assertThat(secondPoint.remainAmounts).isEqualTo(0L)

        assertThat(findFirstPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findFirstPointEvents)
            .extracting("amounts")
            .containsExactly(1L, -1L)
        assertThat(findSecondPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findSecondPointEvents)
            .extracting("amounts")
            .containsExactly(1L, -1L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(SAVE_PAY, SAVE_PAY, USE_CUP)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(1L, 1L, -2L)

        assertThat(findMachine?.cupAmounts).isEqualTo(9)
    }

    @DisplayName("얼음 컵 대여 TC3")
    @Test
    fun rentIceCupTc3() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 1))
        pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 1))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 1))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 1))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_REGISTER_REWARD, 1))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, true)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoints = pointRepository.findAllByUserId(user.id)
        val firstPoint = findPoints[0]
        val secondPoint = findPoints[1]
        val thirdPoint = findPoints[2]
        val findFirstPointEvents = pointEventRepository.findAllByPointId(firstPoint.id)
        val findSecondPointEvents = pointEventRepository.findAllByPointId(secondPoint.id)
        val findThirdPointEvents = pointEventRepository.findAllByPointId(thirdPoint.id)
        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)
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

        assertThat(firstPoint.remainAmounts).isEqualTo(0L)
        assertThat(secondPoint.remainAmounts).isEqualTo(1L)
        assertThat(thirdPoint.remainAmounts).isEqualTo(0L)

        assertThat(findFirstPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findFirstPointEvents)
            .extracting("amounts")
            .containsExactly(1L, -1L)
        assertThat(findSecondPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY)
        assertThat(findSecondPointEvents)
            .extracting("amounts")
            .containsExactly(1L)
        assertThat(findThirdPointEvents)
            .extracting("type")
            .containsExactly(SAVE_REGISTER_REWARD, USE_CUP)
        assertThat(findThirdPointEvents)
            .extracting("amounts")
            .containsExactly(1L, -1L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(SAVE_PAY, SAVE_PAY, SAVE_REGISTER_REWARD, USE_CUP)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(1L, 1L, 1L, -2L)

        assertThat(findMachine?.cupAmounts).isEqualTo(9)
    }

    @DisplayName("컵 대여")
    @Test
    fun rentCup() {
        //given
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        clear()

        //when
        val id = rentalService.rent(user.id, CUP_RFID, vendingMachine.id, false)
        clear()

        //then
        val findRental = rentalRepository.findByIdOrNull(id)
        val findPoint = pointRepository.findAllByUserId(user.id).first()
        val findPointEvents = pointEventRepository.findAllByPointId(findPoint.id)
        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)
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
            .containsExactly(10L, -1L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(10L, -1L)

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

    @DisplayName("대여 내역 조회")
    @Test
    fun getAll() {
        //given
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

        //when
        val result = rentalService.getAll(
            userId = user.id,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result.totalElements).isEqualTo(6)
        assertThat(result.totalPages).isEqualTo(2)
        assertThat(result)
            .extracting("userId")
            .containsExactly(user.id, user.id, user.id)
        assertThat(result)
            .extracting("status")
            .containsExactly(PROCEEDING, PROCEEDING, PROCEEDING)
    }
}