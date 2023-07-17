package com.liah.doribottle.service.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.point.PointRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class PointService(
    private val pointRepository: PointRepository,
    private val pointQueryRepository: PointQueryRepository
) {
    fun save(
        userId: UUID,
        saveType: PointSaveType,
        eventType: PointEventType,
        saveAmounts: Long
    ): UUID {
        val point = pointRepository.save(Point(userId, saveType, eventType, saveAmounts))

        return point.id
    }

    @Transactional(readOnly = true)
    fun getSum(userId: UUID) = pointQueryRepository.getSumByUserId(userId)
}