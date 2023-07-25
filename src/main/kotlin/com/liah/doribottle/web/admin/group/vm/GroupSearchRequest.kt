package com.liah.doribottle.web.admin.group.vm

import com.liah.doribottle.domain.group.GroupType

data class GroupSearchRequest(
    val name: String?,
    val type: GroupType?
)