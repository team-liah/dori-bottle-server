package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.BlockedCauseType
import java.time.Instant
import java.util.*

data class BlockedCauseDto(
    val id: UUID,
    val userId: UUID,
    val type: BlockedCauseType,
    val description: String?,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    val clearPrice: Long = type.clearPrice
}