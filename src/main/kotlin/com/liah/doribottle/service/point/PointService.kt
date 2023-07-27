package com.liah.doribottle.service.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.repository.point.PointHistoryQueryRepository
import com.liah.doribottle.repository.point.PointHistoryRepository
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.service.point.dto.PointHistoryDto
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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
        pointHistoryRepository.save(PointHistory(userId, eventType, saveAmounts))

        return point.id
    }

    @Cacheable(value = ["pointSum"], key = "#userId")
    @Transactional(readOnly = true)
    fun getSum(userId: UUID) = pointQueryRepository.getSumByUserId(userId)

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