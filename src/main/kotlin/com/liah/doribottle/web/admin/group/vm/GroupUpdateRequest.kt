package com.liah.doribottle.web.admin.group.vm

import com.liah.doribottle.domain.group.GroupType
import jakarta.validation.constraints.NotNull

data class GroupUpdateRequest(
    @field:NotNull
    val name: String?,
    @field:NotNull
    val type: GroupType?
)