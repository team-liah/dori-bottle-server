package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.BlockedCauseType
import java.util.*

data class BlockedCauseDto(
    val id: UUID,
    val userId: UUID,
    val type: BlockedCauseType,
    val description: String?
) {
    val clearPrice: Long = type.clearPrice
}