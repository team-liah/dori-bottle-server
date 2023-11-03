package com.liah.doribottle.service.point.dto

import com.liah.doribottle.domain.point.PointSaveType
import java.time.Instant
import java.util.*

data class PointDto(
    val id: UUID,
    val userId: UUID,
    val saveType: PointSaveType,
    val description: String,
    val saveAmounts: Long,
    val remainAmounts: Long,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)