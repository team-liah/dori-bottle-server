package com.liah.doribottle.service.point

import com.liah.doribottle.domain.point.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class PointService(
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository
) {
    fun save(
        userId: UUID,
        saveType: PointSaveType,
        historyType: PointHistoryType,
        saveAmounts: Long,
        description: String
    ): UUID {
        val point = pointRepository.save(Point(userId, saveType, description, saveAmounts))
        pointHistoryRepository.save(PointHistory(userId, point, historyType, description, saveAmounts))

        return point.id
    }
}