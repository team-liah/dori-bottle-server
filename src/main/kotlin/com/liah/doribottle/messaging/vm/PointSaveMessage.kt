package com.liah.doribottle.messaging.vm

import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import java.util.UUID

data class PointSaveMessage(
    var userId: UUID? = null,
    var saveType: PointSaveType? = null,
    var eventType: PointEventType? = null,
    var saveAmounts: Long? = null,
)
