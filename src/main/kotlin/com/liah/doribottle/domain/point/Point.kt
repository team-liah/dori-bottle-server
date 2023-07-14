package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.point.PointEventType.USE_CUP
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
    userId: UUID,
    saveType: PointSaveType,
    eventType: PointEventType,
    saveAmounts: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val userId: UUID = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val saveType: PointSaveType = saveType

    @Column(nullable = false, length = 1000)
    val description: String = eventType.title

    @Column(nullable = false)
    val saveAmounts: Long = saveAmounts

    @Column(nullable = false)
    var remainAmounts: Long = saveAmounts
        protected set

    @OneToMany(mappedBy = "point", fetch = LAZY, cascade = [ALL])
    protected val mutableEvents: MutableList<PointEvent> = mutableListOf()

    init {
        mutableEvents.add(PointEvent(this, eventType, saveAmounts))
    }

    fun use(amounts: Long): Long {
        val remain = remainAmounts - amounts
        return if (remain >= 0) {
            mutableEvents.add(PointEvent(this, USE_CUP, amounts))
            remainAmounts = remain
            0
        } else {
            mutableEvents.add(PointEvent(this, USE_CUP, remainAmounts))
            remainAmounts = 0
            -remain
        }
    }
}