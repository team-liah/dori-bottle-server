package com.liah.doribottle.web.admin.group.vm

import com.liah.doribottle.domain.group.GroupType
import java.util.UUID

data class GroupSearchResponse(
    val id: UUID,
    val name: String,
    val type: GroupType,
    val discountRate: Int
)
