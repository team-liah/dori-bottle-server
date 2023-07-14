package com.liah.doribottle.service.rental

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointEventType.SAVE_PAY
import com.liah.doribottle.domain.point.PointEventType.USE_CUP
import com.liah.doribottle.domain.point.PointSaveType.PAY
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
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
        private const val MACHINE_NO = "000-00000"
    }

    private lateinit var user: User
    private lateinit var cup: Cup
    private lateinit var vendingMachine: Machine

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))

        cup = cupRepository.save(Cup(CUP_RFID))

        vendingMachine = machineRepository.save(Machine(MACHINE_NO, VENDING, Address(),100))

        clear()
    }

    @DisplayName("컵 대여")
    @Test
    fun rental() {
        //given, when
        val id = rentalService.rental(user.id, CUP_RFID, vendingMachine.id, true)
        clear()

        //then
        val rental = rentalRepository.findByIdOrNull(id)
        val point = pointRepository.findByUserId(user.id)
        val pointEvents = pointEventRepository.findAllByPointId(point?.id!!)

        assertThat(rental?.user).isEqualTo(user)
        assertThat(rental?.cup).isEqualTo(cup)
        assertThat(rental?.fromMachine).isEqualTo(vendingMachine)
        assertThat(rental?.toMachine).isNull()
        assertThat(rental?.withIce).isEqualTo(true)
        assertThat(rental?.cost).isEqualTo(2L)
        assertThat(rental?.succeededDate).isNull()
        assertThat(rental?.expiredDate).isAfter(Instant.now())
        assertThat(rental?.status).isEqualTo(PROCEEDING)

        assertThat(point.remainAmounts).isEqualTo(8L)
        assertThat(pointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(pointEvents)
            .extracting("amounts")
            .containsExactly(10L, 2L)
    }
}