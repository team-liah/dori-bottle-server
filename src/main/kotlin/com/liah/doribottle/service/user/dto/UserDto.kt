package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import java.util.*

data class UserDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role
)