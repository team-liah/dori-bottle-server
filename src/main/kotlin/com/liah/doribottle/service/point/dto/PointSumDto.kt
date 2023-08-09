package com.liah.doribottle.service.point.dto

import com.liah.doribottle.web.v1.point.vm.RemainPointResponse
import com.querydsl.core.annotations.QueryProjection
import java.util.*

data class PointSumDto @QueryProjection constructor(
    val userId : UUID? = null,
    val totalPayAmounts: Long = 0,
    val totalRewordAmounts: Long = 0
) {
    fun toRemainPoint() = RemainPointResponse(totalPayAmounts, totalRewordAmounts)
}