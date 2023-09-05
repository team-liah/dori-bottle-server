package com.liah.doribottle.service.point

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType.PAY
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
import org.junit.jupiter.api.assertThrows
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

        assertThat(cacheManager.getCache("pointSum")?.get(user.id)).isNull()
    }

    @DisplayName("포인트 만료")
    @Test
    fun expire() {
        val point = pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        clear()

        pointService.expire(point.id, user.id)
        clear()

        val findPoint = pointRepository.findByIdOrNull(point.id)

        val findPointEvents = pointEventRepository.findAllByPointId(point.id)

        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)

        assertThat(findPoint?.remainAmounts).isEqualTo(0)

        assertThat(findPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, CANCEL_SAVE)
        assertThat(findPointEvents)
            .extracting("amounts")
            .containsExactly(10L, -10L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(CANCEL_SAVE)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(-10L)

        assertThat(cacheManager.getCache("pointSum")?.get(user.id)).isNull()
    }

    @DisplayName("포인트 사용")
    @Test
    fun use() {
        //given
        val rewardPoint = pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        val payPoint = pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        given(mockPointQueryRepository.getAllRemainByUserId(user.id))
            .willReturn(listOf(rewardPoint, payPoint))

        //when
        pointService.use(user.id, 15)
        clear()

        //then
        verify(mockPointQueryRepository, times(1)).getAllRemainByUserId(user.id)

        val findRewardPoint = pointRepository.findByIdOrNull(rewardPoint.id)
        val findPayPoint = pointRepository.findByIdOrNull(payPoint.id)

        val findRewardPointEvents = pointEventRepository.findAllByPointId(rewardPoint.id)
        val findPayPointEvents = pointEventRepository.findAllByPointId(payPoint.id)

        val findPointHistories = pointHistoryRepository.findAllByUserId(user.id)

        assertThat(findRewardPoint?.remainAmounts).isEqualTo(0)
        assertThat(findPayPoint?.remainAmounts).isEqualTo(5)

        assertThat(findRewardPointEvents)
            .extracting("type")
            .containsExactly(SAVE_REGISTER_REWARD, USE_CUP)
        assertThat(findRewardPointEvents)
            .extracting("amounts")
            .containsExactly(10L, -10L)
        assertThat(findPayPointEvents)
            .extracting("type")
            .containsExactly(SAVE_PAY, USE_CUP)
        assertThat(findPayPointEvents)
            .extracting("amounts")
            .containsExactly(10L, -5L)

        assertThat(findPointHistories)
            .extracting("eventType")
            .containsExactly(USE_CUP)
        assertThat(findPointHistories)
            .extracting("amounts")
            .containsExactly(-15L)

        assertThat(cacheManager.getCache("pointSum")?.get(user.id)).isNull()
    }

    @DisplayName("포인트 사용 예외")
    @Test
    fun useException() {
        val rewardPoint = pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        val payPoint = pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        given(mockPointQueryRepository.getAllRemainByUserId(user.id))
            .willReturn(listOf(rewardPoint, payPoint))

        val exception = assertThrows<BusinessException> {
            pointService.use(user.id, 25)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.LACK_OF_POINT)
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

    @DisplayName("유저 잔여 포인트 목록 조회")
    @Test
    fun getAllRemainByUserId() {
        //given
        val rewardPoint = pointRepository.save(Point(user.id, REWARD, SAVE_REGISTER_REWARD, 10))
        val payPoint = pointRepository.save(Point(user.id, PAY, SAVE_PAY, 10))
        given(mockPointQueryRepository.getAllRemainByUserId(user.id))
            .willReturn(listOf(rewardPoint, payPoint))
        clear()

        //when
        val result = pointService.getAllRemainByUserId(user.id)

        //then
        verify(mockPointQueryRepository, times(1)).getAllRemainByUserId(user.id)
        assertThat(result)
            .extracting("userId")
            .containsExactly(user.id, user.id)
        assertThat(result)
            .extracting("saveType")
            .containsExactly(REWARD, PAY)
        assertThat(result)
            .extracting("saveAmounts")
            .containsExactly(10L, 10L)
        assertThat(result)
            .extracting("remainAmounts")
            .containsExactly(10L, 10L)
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