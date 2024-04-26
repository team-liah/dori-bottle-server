package com.liah.doribottle.repository.point

import com.liah.doribottle.config.TestConfig
import com.liah.doribottle.config.TestcontainersConfig
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Import
import org.springframework.stereotype.Repository
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("test")
@Import(TestConfig::class, TestcontainersConfig::class)
@DataJpaTest(includeFilters = [ComponentScan.Filter(type = FilterType.ANNOTATION, classes = [Repository::class])])
class PointQueryRepositoryTest {
    @PersistenceContext private lateinit var entityManager: EntityManager
    @Autowired private lateinit var pointQueryRepository: PointQueryRepository
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var userRepository: UserRepository

    private lateinit var user: User

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @BeforeEach
    internal fun init() {
        val userEntity = User("010-5638-3316", "Tester 1", "010-5638-3316", Role.USER)
        user = userRepository.save(userEntity)
        clear()
    }

    @DisplayName("유저 잔여 포인트 전체 조회")
    @Test
    fun getAllRemainByUserId() {
        //given
        pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 20))
        pointRepository.save(Point(user.id, REWARD, SAVE_INVITE_REWARD, 30))
        pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_INVITER_REWARD, 0))
        clear()

        //when
        val result = pointQueryRepository.getAllRemainByUserId(user.id)

        //then
        assertThat(result)
            .extracting("saveType")
            .containsExactly(REWARD, REWARD, PAY)
        assertThat(result)
            .extracting("remainAmounts")
            .containsExactly(10L, 30L, 20L)
    }

    @DisplayName("유저 포인트 총합 조회")
    @Test
    fun getSumByUserId() {
        //given
        pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        pointRepository.save(Point(user.id, PAY, SAVE_PAY, 20))
        clear()

        //when
        val result = pointQueryRepository.getSumByUserId(user.id)

        //then
        assertThat(result.userId).isEqualTo(user.id)
        assertThat(result.totalRewordAmounts).isEqualTo(10)
        assertThat(result.totalPayAmounts).isEqualTo(20)
    }
}