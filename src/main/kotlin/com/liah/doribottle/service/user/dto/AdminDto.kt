package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Role
import java.time.Instant
import java.util.*

data class AdminDto(
    val id: UUID,
    val loginId: String,
    val loginPassword: String,
    val name: String,
    val role: Role,
    val deleted: Boolean,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)