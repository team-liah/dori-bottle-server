package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.util.*

@Entity
@Table(
    name = "point_event",
    indexes = [Index(name = "IDX_POINT_EVENT_POINT_ID", columnList = "point_id")]
)
class PointEvent(
    point: Point,
    type: PointEventType,
    amounts: Long,
    targetId: UUID?
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "point_id", nullable = false)
    val point: Point = point

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointEventType = type

    @Column(nullable = false)
    val amounts: Long = amounts

    val targetId: UUID? = targetId

    fun cancel(): Long {
        return point.cancel(this)
    }
}