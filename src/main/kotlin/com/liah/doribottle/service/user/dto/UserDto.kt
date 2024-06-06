package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.group.dto.GroupDto
import java.time.Instant
import java.util.UUID

data class UserDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val invitationCode: String,
    val birthDate: String?,
    val gender: Gender?,
    val role: Role,
    val active: Boolean,
    val registeredDate: Instant?,
    val group: GroupDto?,
    val createdDate: Instant,
    val lastModifiedDate: Instant,
)
