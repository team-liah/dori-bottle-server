package com.liah.doribottle.service.point

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.point.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class PointService(
    private val pointRepository: PointRepository,
    private val pointHistoryRepository: PointHistoryRepository,
    private val pointSumRepository: PointSumRepository
) {
    fun getSum(userId: UUID) =
        pointSumRepository.findByUserId(userId)?.toDto()
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

    fun save(
        userId: UUID,
        saveType: PointSaveType,
        historyType: PointHistoryType,
        saveAmounts: Long,
        description: String = historyType.title
    ): UUID {
        val pointSum = pointSumRepository.findByUserId(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        pointSum.plusAmounts(saveType, saveAmounts)

        val point = pointRepository.save(Point(userId, saveType, description, saveAmounts))
        pointHistoryRepository.save(PointHistory(userId, point, historyType, description, saveAmounts))

        return point.id
    }
}