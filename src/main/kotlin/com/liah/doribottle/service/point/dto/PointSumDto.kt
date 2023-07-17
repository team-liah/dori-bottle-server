package com.liah.doribottle.service.point.dto

import com.liah.doribottle.web.me.vm.RemainPointResponse
import com.querydsl.core.annotations.QueryProjection
import java.util.*

data class PointSumDto @QueryProjection constructor(
    val userId : UUID,
    val totalPayAmounts: Long,
    val totalRewordAmounts: Long
) {
    fun toRemainPoint() = RemainPointResponse(totalPayAmounts, totalRewordAmounts)
}