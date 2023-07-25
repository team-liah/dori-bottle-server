package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.point.dto.PointHistoryDto
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "point_history",
    indexes = [Index(name = "IDX_POINT_HISTORY_USER_ID", columnList = "userId")]
)
class PointHistory(
    userId: UUID,
    type: PointEventType,
    amounts: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val userId: UUID = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val eventType: PointEventType = type

    @Column(nullable = false)
    val amounts: Long = amounts

    fun toDto() = PointHistoryDto(id, userId, eventType, amounts, createdDate, lastModifiedDate)
}