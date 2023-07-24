package com.liah.doribottle.service.point.dto

import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.web.v1.point.vm.PointHistorySearchResponse
import java.time.Instant
import java.util.*

data class PointHistoryDto(
    val id: UUID,
    val userId: UUID,
    val eventType: PointEventType,
    val amounts: Long,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toResponse() = PointHistorySearchResponse(id, userId, eventType, eventType.title, amounts, createdDate, lastModifiedDate)
}