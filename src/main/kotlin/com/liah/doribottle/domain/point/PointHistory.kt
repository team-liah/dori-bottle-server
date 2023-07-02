package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY
import java.util.*

@Entity
@Table(
    name = "point_history",
    indexes = [Index(name = "IDX_POINT_HISTORY_USER_ID", columnList = "userId")]
)
class PointHistory(
    userId: UUID,
    point: Point,
    type: PointHistoryType,
    description: String,
    amounts: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val userId: UUID = userId

    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(nullable = false)
    val point: Point = point

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: PointHistoryType = type

    @Column(nullable = false, length = 1000)
    val description: String = description

    @Column(nullable = false)
    val amounts: Long = amounts
}