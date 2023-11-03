package com.liah.doribottle.web.admin.admin.vm

import com.liah.doribottle.domain.user.Role

data class AdminSearchRequest(
    val loginId: String?,
    val name: String?,
    val role: Role?,
    val deleted: Boolean?
)