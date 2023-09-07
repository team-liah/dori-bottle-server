package com.liah.doribottle.service.point

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointEventType.CANCEL_SAVE
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.event.Events
import com.liah.doribottle.repository.point.PointHistoryQueryRepository
import com.liah.doribottle.repository.point.PointHistoryRepository
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.service.point.dto.PointDto
import com.liah.doribottle.service.point.dto.PointHistoryDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class PointService(
    private val pointRepository: PointRepository,
    private val pointQueryRepository: PointQueryRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointHistoryQueryRepository: PointHistoryQueryRepository
) {
    @CacheEvict(value = ["pointSum"], key = "#userId")
    fun save(
        userId: UUID,
        saveType: PointSaveType,
        eventType: PointEventType,
        saveAmounts: Long
    ): UUID {
        val point = pointRepository.save(Point(userId, saveType, eventType, saveAmounts))
        val pointHistory = pointHistoryRepository.save(PointHistory(userId, eventType, saveAmounts))

        Events.notify(
            userId = userId,
            type = NotificationType.POINT,
            content = "${eventType.title} 버블 ${saveAmounts}개가 지급되었습니다.",
            targetId = pointHistory.id
        )

        return point.id
    }

    @CacheEvict(value = ["pointSum"], key = "#userId")
    fun expire(
        id: UUID,
        userId: UUID
    ) {
        val point = pointRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.POINT_NOT_FOUNT)
        val pointHistory = pointHistoryRepository.save(PointHistory(userId, CANCEL_SAVE, -point.remainAmounts))

        point.expire()

        Events.notify(
            userId = userId,
            type = NotificationType.REFUND,
            content = "버블 ${point.remainAmounts}개 환불 요청이 처리되었습니다.",
            targetId = pointHistory.id
        )
    }

    @CacheEvict(value = ["pointSum"], key = "#userId")
    fun use(
        userId: UUID,
        useAmounts: Long
    ) {
        val points = pointQueryRepository.getAllRemainByUserId(userId)
        var remainAmounts = useAmounts
        points.forEach { point ->
            remainAmounts = point.use(remainAmounts)
            if (remainAmounts == 0L) {
                pointHistoryRepository.save(PointHistory(userId, PointEventType.USE_CUP, -useAmounts))
                return
            }
        }

        throw BusinessException(ErrorCode.LACK_OF_POINT)
    }

    @Cacheable(value = ["pointSum"], key = "#userId")
    @Transactional(readOnly = true)
    fun getSum(userId: UUID) = pointQueryRepository.getSumByUserId(userId)

    @Transactional(readOnly = true)
    fun getAllRemainByUserId(
        userId: UUID
    ): List<PointDto> {
        return pointQueryRepository.getAllRemainByUserId(
            userId = userId
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAllHistories(
        userId: UUID? = null,
        eventType: PointEventType? = null,
        pageable: Pageable
    ): Page<PointHistoryDto> {
        return pointHistoryQueryRepository.getAll(
            userId = userId,
            eventType = eventType,
            pageable = pageable
        ).map { it.toDto() }
    }
}