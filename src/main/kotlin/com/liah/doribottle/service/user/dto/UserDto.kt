package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Role
import java.util.*

data class UserDto(
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationKey: UUID,
    val active: Boolean,
    val blocked: Boolean,
    val role: Role
)