package com.liah.doribottle.service.point.dto

import com.liah.doribottle.domain.point.PointEventType
import java.util.*

data class PointHistoryDto(
    val id: UUID,
    val userId: UUID,
    val type: PointEventType,
    val amounts: Long
)
