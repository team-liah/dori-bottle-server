package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.web.me.vm.ProfileResponse
import java.util.*

data class UserDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: Int?,
    val gender: Gender?,
    val role: Role
) {
    fun toProfile() = ProfileResponse(id, loginId, name, phoneNumber, invitationCode, birthDate, gender, role)
}