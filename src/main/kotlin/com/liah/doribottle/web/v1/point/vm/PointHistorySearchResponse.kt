package com.liah.doribottle.web.v1.point.vm

import com.liah.doribottle.domain.point.PointEventType
import java.time.Instant
import java.util.*

data class PointHistorySearchResponse(
    val id: UUID,
    val userId: UUID,
    val eventType: PointEventType,
    val eventTitle: String,
    val amounts: Long,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)