package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import java.time.Instant
import java.util.*

data class AdminDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role,
    val email: String?,
    val phoneNumber: String?,
    val description: String?,
    val gender: Gender?,
    val deleted: Boolean,
    val createdDate: Instant,
    val lastModifiedDate: Instant,
)
