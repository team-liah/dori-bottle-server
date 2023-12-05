package com.liah.doribottle.web.admin.user.vm

import java.util.*

data class UserUpdateRequest(
    val groupId: UUID?,
    val description: String?
)
