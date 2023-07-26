package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.group.dto.GroupDto
import com.liah.doribottle.web.admin.user.vm.UserSearchResponse
import java.util.*

data class UserDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role,
    val group: GroupDto?
) {
    fun toSearchResponse() = UserSearchResponse(id, loginId, name, phoneNumber, invitationCode, birthDate, gender, role, group)
}