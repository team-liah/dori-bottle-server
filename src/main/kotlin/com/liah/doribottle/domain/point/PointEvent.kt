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
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "point_id", nullable = false)
    val point: Point,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointEventType,

    @Column(nullable = false)
    val amounts: Long,

    @Column
    val targetId: UUID? = null
) : PrimaryKeyEntity() {
    fun cancel(): Long {
        return point.cancel(this)
    }
}