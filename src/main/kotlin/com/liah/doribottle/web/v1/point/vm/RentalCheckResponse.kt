package com.liah.doribottle.web.v1.point.vm

import com.liah.doribottle.constant.DoriConstant
import java.time.Instant
import java.time.temporal.ChronoUnit

data class RentalCheckResponse private constructor(
    val possible: Int,
    val expectedReturnDate: Instant
) {
    constructor(totalPoint: Long): this(
        when {
            totalPoint >= DoriConstant.RENT_ICE_CUP_AMOUNTS -> RentalCheckType.ICE_CUP_AVAILABLE.possible
            totalPoint >= DoriConstant.RENT_CUP_AMOUNTS -> RentalCheckType.CUP_AVAILABLE.possible
            else -> RentalCheckType.NOT_AVAILABLE.possible
        },
        Instant.now().plus(DoriConstant.RENT_CUP_HOUR_LIMIT, ChronoUnit.HOURS)
    )

    private enum class RentalCheckType(
        val possible: Int
    ) {
        NOT_AVAILABLE(0),    // 대여 불가
        CUP_AVAILABLE(1),    // 컵 대여 가능
        ICE_CUP_AVAILABLE(2) // 얼음컵 대여 가능
    }
}