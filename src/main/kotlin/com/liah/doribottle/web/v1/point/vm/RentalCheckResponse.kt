package com.liah.doribottle.web.v1.point.vm

import com.liah.doribottle.constant.DoriConstant
import java.time.Instant
import java.time.temporal.ChronoUnit

data class RentalCheckResponse private constructor(
    // 0 -> 대여 불가, 1 -> 컵 대여 가능, 2 -> 얼음컵 대여 가능
    val possible: Int,
    val expectedReturnDate: Instant
) {
    constructor(totalPoint: Long): this(
        when {
            totalPoint >= DoriConstant.RENT_ICE_CUP_AMOUNTS -> 2
            totalPoint >= DoriConstant.RENT_CUP_AMOUNTS -> 1
            else -> 0
        },
        Instant.now().plus(DoriConstant.RENT_CUP_HOUR_LIMIT, ChronoUnit.HOURS)
    )
}