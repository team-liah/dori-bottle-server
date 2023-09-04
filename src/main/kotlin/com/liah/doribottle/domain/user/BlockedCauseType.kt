package com.liah.doribottle.domain.user

import com.liah.doribottle.constant.FIVE_PENALTIES_PRICE
import com.liah.doribottle.constant.LOST_CUP_PRICE

enum class BlockedCauseType(
    val description: String,
    val clearPrice: Long
) {
    FIVE_PENALTIES("페널티 5개 이상", FIVE_PENALTIES_PRICE),
    LOST_CUP_PENALTY("분실 컵 페널티", LOST_CUP_PRICE)
}