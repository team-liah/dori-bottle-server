package com.liah.doribottle.event.point

import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import java.util.*

data class PointSaveEvent(
    val userId: UUID,
    val saveType: PointSaveType,
    val eventType: PointEventType,
    val saveAmounts: Long
)