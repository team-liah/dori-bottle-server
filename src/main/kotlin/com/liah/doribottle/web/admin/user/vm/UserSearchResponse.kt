package com.liah.doribottle.web.admin.user.vm

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.group.dto.GroupDto
import java.util.*

data class UserSearchResponse(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role,
    val group: GroupDto?
)