package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "point",
    indexes = [Index(name = "IDX_POINT_USER_ID", columnList = "userId")]
)
class Point(
    userId: UUID,
    saveType: PointSaveType,
    description: String,
    saveAmounts: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val userId: UUID = userId

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val saveType: PointSaveType = saveType

    @Column(nullable = false, length = 1000)
    val description: String = description

    @Column(nullable = false)
    val saveAmounts: Long = saveAmounts

    @Column(nullable = false)
    var remainAmounts: Long = saveAmounts
        protected set

    fun use(amounts: Long): Long {
        val remain = remainAmounts - amounts
        return if (remain >= 0) {
            remainAmounts = remain
            0
        } else {
            remainAmounts = 0
            -remain
        }
    }
}