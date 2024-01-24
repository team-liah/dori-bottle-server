package com.liah.doribottle.domain.user

import com.liah.doribottle.constant.DoriConstant

enum class BlockedCauseType(
    val description: String,
    val clearPrice: Long
) {
    FIVE_PENALTIES("페널티 5개 이상", DoriConstant.FIVE_PENALTIES_PRICE),
    LOST_CUP_PENALTY("분실 컵 페널티", DoriConstant.LOST_CUP_PRICE)
}