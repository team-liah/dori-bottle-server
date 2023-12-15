package com.liah.doribottle.service.group.dto

import com.liah.doribottle.domain.group.GroupType
import java.time.Instant
import java.util.*

data class GroupDto(
    val id: UUID,
    val code: String,
    val name: String,
    val type: GroupType,
    val discountRate: Int,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)
