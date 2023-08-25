package com.liah.doribottle.service.point

import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.point.PointEventRepository
import com.liah.doribottle.repository.point.PointHistoryRepository
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.point.dto.PointSumDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.given
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.cache.CacheManager
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class PointServiceTest : BaseServiceTest() {
    @Autowired private lateinit var pointService: PointService
    @Autowired private lateinit var pointRepository: PointRepository
    @Autowired private lateinit var pointEventRepository: PointEventRepository
    @Autowired private lateinit var pointHistoryRepository: PointHistoryRepository
    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var cacheManager: CacheManager

    @MockBean private lateinit var mockPointQueryRepository: PointQueryRepository

    private lateinit var user: User

    @BeforeEach
    internal fun init() {
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        user = userRepository.save(userEntity)
        clear()
    }

    @DisplayName("포인트 총합 조회")
    @Test
    fun getSum() {
        //given
        val pointSumDto = PointSumDto(user.id, 10, 20)
        given(mockPointQueryRepository.getSumByUserId(user.id))
            .willReturn(pointSumDto)

        //when
        val sum = pointService.getSum(user.id)

        //then
        assertThat(sum.userId).isEqualTo(user.id)
        assertThat(sum.totalPayAmounts).isEqualTo(10)
        assertThat(sum.totalRewordAmounts).isEqualTo(20)
    }

    @DisplayName("포인트 총합 조회 - 캐싱")
    @Test
    fun getSumCache() {
        //given
        val pointSumDto = PointSumDto(user.id, 10, 20)
        given(mockPointQueryRepository.getSumByUserId(user.id))
            .willReturn(pointSumDto)

        //when
        val pointSumMiss = pointService.getSum(user.id)
        val pointSumHit = pointService.getSum(user.id)

        //then
        assertThat(pointSumMiss).isEqualTo(pointSumDto)
        assertThat(pointSumHit).isEqualTo(pointSumDto)

        verify(mockPointQueryRepository, times(1)).getSumByUserId(user.id)
        assertThat(cacheManager.getCache("pointSum")?.get(user.id)?.get()).isEqualTo(pointSumDto)
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
        val findPointEvents = pointEventRepository.findAllByPointId(id)
        val findPointHistories = pointHistoryRepository.findAllByUserId(userId)

        assertThat(findPoint?.userId).isEqualTo(userId)
        assertThat(findPoint?.saveType).isEqualTo(REWARD)
        assertThat(findPoint?.saveAmounts).isEqualTo(SAVE_REGISTER_REWARD_AMOUNTS)
        assertThat(findPoint?.description).isEqualTo(SAVE_REGISTER_REWARD.title)

        assertThat(findPointEvents)
            .extracting("type")
            .containsExactly(SAVE_REGISTER_REWARD)
        assertThat(findPointEvents)
            .extracting("amounts")
            .containsExactly(SAVE_REGISTER_REWARD_AMOUNTS)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(SAVE_REGISTER_REWARD)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(SAVE_REGISTER_REWARD_AMOUNTS)
    }

    @DisplayName("포인트 내역 조회")
    @Test
    fun getAllHistories() {
        //given
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, USE_CUP, -2))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_PAY, 10))
        pointHistoryRepository.save(PointHistory(user.id, SAVE_INVITE_REWARD, 10))
        pointHistoryRepository.save(PointHistory(user.id, USE_CUP, -2))
        clear()

        //when
        val result = pointService.getAllHistories(
            userId = user.id,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result.totalElements).isEqualTo(6)
        assertThat(result.totalPages).isEqualTo(2)
        assertThat(result)
            .extracting("eventType")
            .containsExactly(SAVE_PAY, SAVE_PAY, USE_CUP)
        assertThat(result)
            .extracting("amounts")
            .containsExactly(10L, 10L, -2L)
    }
}