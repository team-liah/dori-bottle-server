package com.liah.doribottle.service.group.dto

import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.web.admin.group.vm.GroupSearchResponse
import java.util.UUID

data class GroupDto(
    val id: UUID,
    val name: String,
    val type: GroupType,
    val discountRate: Int
) {
    fun toSearchResponse() = GroupSearchResponse(id, name, type)
}
