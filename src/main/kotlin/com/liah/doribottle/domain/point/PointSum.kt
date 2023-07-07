package com.liah.doribottle.domain.point

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.point.dto.PointSumDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(
    name = "point_sum",
    indexes = [Index(name = "IDX_POINT_SUM_USER_ID", columnList = "userId")]
)
class PointSum(
    userId: UUID,
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val userId: UUID = userId

    @Column(nullable = false)
    var totalPayAmounts: Long = 0
        protected set

    @Column(nullable = false)
    var totalRewordAmounts: Long = 0
        protected set

    fun plusAmounts(type: PointSaveType, amounts: Long) {
        when (type) {
            PointSaveType.PAY -> this.totalPayAmounts += amounts
            PointSaveType.REWARD -> this.totalRewordAmounts += amounts
        }
    }

    fun toDto() = PointSumDto(userId, totalPayAmounts, totalRewordAmounts)
}