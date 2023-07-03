package com.liah.doribottle.event.point

import com.liah.doribottle.domain.point.PointHistoryType
import com.liah.doribottle.domain.point.PointSaveType
import java.util.*

data class PointSaveEvent(
    val userId: UUID,
    val saveType: PointSaveType,
    val historyType: PointHistoryType,
    val saveAmounts: Long,
    val description: String = historyType.title
)