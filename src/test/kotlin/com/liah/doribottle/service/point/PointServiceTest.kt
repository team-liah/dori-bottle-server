package com.liah.doribottle.service.point

import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointEventType.SAVE_REGISTER_REWARD
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.point.PointSum
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.point.PointEventRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.point.PointSumRepository
import com.liah.doribottle.repository.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional
import java.util.*

@SpringBootTest
@Transactional
class PointServiceTest {
    @PersistenceContext private lateinit var entityManager: EntityManager
    @Autowired private lateinit var pointService: PointService
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var pointEventRepository: PointEventRepository
    @Autowired private lateinit var pointSumRepository: PointSumRepository
    @Autowired private lateinit var userRepository: UserRepository

    companion object {
        private const val USER_LOGIN_ID = "010-5638-3316"
    }

    private lateinit var user: User

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
        pointSumRepository.save(PointSum(user.id))
        clear()
    }

    @DisplayName("포인트 총합 조회")
    @Test
    fun getSum() {
        //given
        val userId = user.id

        //when
        val sum = pointService.getSum(userId)

        //then
        assertThat(sum.totalPayAmounts).isEqualTo(0)
        assertThat(sum.totalRewordAmounts).isEqualTo(0)
    }

    @DisplayName("적립")
    @Test
    fun save() {
        //given
        val userId = user.id

        //when
        val id = pointService.save(userId, REWARD, SAVE_REGISTER_REWARD, SAVE_REGISTER_REWARD_AMOUNTS)
        clear()

        //then
        val findPoint = pointRepository.findByIdOrNull(id)
        val findSumPoint = pointSumRepository.findByUserId(userId)
        val findPointEvents = pointEventRepository.findAllByPointId(id)

        assertThat(findPoint?.userId).isEqualTo(userId)
        assertThat(findPoint?.saveType).isEqualTo(REWARD)
        assertThat(findPoint?.saveAmounts).isEqualTo(SAVE_REGISTER_REWARD_AMOUNTS)
        assertThat(findPoint?.description).isEqualTo(SAVE_REGISTER_REWARD.title)

        assertThat(findSumPoint?.totalPayAmounts).isEqualTo(0L)
        assertThat(findSumPoint?.totalRewordAmounts).isEqualTo(SAVE_REGISTER_REWARD_AMOUNTS)

        assertThat(findPointEvents)
            .extracting("type")
            .containsExactly(SAVE_REGISTER_REWARD)
        assertThat(findPointEvents)
            .extracting("amounts")
            .containsExactly(SAVE_REGISTER_REWARD_AMOUNTS)
    }
}