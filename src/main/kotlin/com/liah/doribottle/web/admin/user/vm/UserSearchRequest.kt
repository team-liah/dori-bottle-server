package com.liah.doribottle.web.admin.user.vm

import com.liah.doribottle.domain.user.Gender
import java.util.*

data class UserSearchRequest(
    val name: String?,
    val phoneNumber: String?,
    val birthDate: String?,
    val gender: Gender?,
    val active: Boolean?,
    val blocked: Boolean?,
    val groupId: UUID?
)