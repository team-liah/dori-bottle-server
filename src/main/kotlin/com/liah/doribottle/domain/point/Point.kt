package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.point.PointEventType.*
import com.liah.doribottle.service.point.dto.PointDto
import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.FetchType.LAZY
import java.util.*

@Entity
@Table(
    name = "point",
    indexes = [Index(name = "IDX_POINT_USER_ID", columnList = "userId")]
)
class Point(
    @Column(nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.ORDINAL)
    @Column(nullable = false)
    val saveType: PointSaveType,

    eventType: PointEventType,

    @Column(nullable = false)
    val saveAmounts: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false, length = 1000)
    val description: String = eventType.title

    @Column(nullable = false)
    var remainAmounts: Long = saveAmounts
        protected set

    @OneToMany(mappedBy = "point", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
    protected val mutableEvents: MutableList<PointEvent> = mutableListOf()
    val events: List<PointEvent> get() = mutableEvents

    init {
        mutableEvents.add(PointEvent(this, eventType, saveAmounts, null))
    }

    fun use(amounts: Long, targetId: UUID): Long {
        val remain = remainAmounts - amounts
        return if (remain >= 0) {
            mutableEvents.add(PointEvent(this, USE_CUP, -amounts, targetId))
            remainAmounts = remain
            0
        } else {
            mutableEvents.add(PointEvent(this, USE_CUP, -remainAmounts, targetId))
            remainAmounts = 0
            -remain
        }
    }

    fun cancel(event: PointEvent): Long {
        val cancelAmounts = -1 * event.amounts
        mutableEvents.add(PointEvent(this, CANCEL_USE, cancelAmounts, null))
        remainAmounts += cancelAmounts
        return cancelAmounts
    }

    fun expire() {
        mutableEvents.add(PointEvent(this, CANCEL_SAVE, -remainAmounts, null))
        remainAmounts = 0
    }

    fun toDto() = PointDto(id, userId, saveType, description, saveAmounts, remainAmounts, createdDate, lastModifiedDate)
}