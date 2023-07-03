package com.liah.doribottle.service.point

import com.liah.doribottle.domain.point.PointHistoryRepository
import com.liah.doribottle.domain.point.PointHistoryType.SAVE_REGISTER_REWARD
import com.liah.doribottle.domain.point.PointRepository
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
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
    @Autowired private lateinit var pointHistoryRepository: PointHistoryRepository

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("적립")
    @Test
    fun save() {
        //given
        val userId = UUID.randomUUID()

        //when
        val id = pointService.save(userId, REWARD, SAVE_REGISTER_REWARD, 10)
        clear()

        //then
        val findPoint = pointRepository.findByIdOrNull(id)
        val findPointHistories = pointHistoryRepository.findAllByPointId(id)
        assertThat(findPoint?.userId).isEqualTo(userId)
        assertThat(findPoint?.saveType).isEqualTo(REWARD)
        assertThat(findPoint?.saveAmounts).isEqualTo(10L)
        assertThat(findPoint?.description).isEqualTo(SAVE_REGISTER_REWARD.title)
        assertThat(findPointHistories)
            .extracting("type")
            .containsExactly(SAVE_REGISTER_REWARD)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(10L)
        assertThat(findPointHistories)
            .extracting("description")
            .containsExactly(SAVE_REGISTER_REWARD.title)
    }
}