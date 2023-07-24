package com.liah.doribottle.service.group.dto

import com.liah.doribottle.domain.group.GroupType
import java.util.UUID

data class GroupDto(
    val id: UUID,
    val name: String,
    val type: GroupType
)
