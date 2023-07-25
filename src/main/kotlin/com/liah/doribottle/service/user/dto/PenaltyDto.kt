package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.PenaltyType
import java.util.*

data class PenaltyDto(
    val id: UUID,
    val userId: UUID,
    val type: PenaltyType,
    val cause: String?
)