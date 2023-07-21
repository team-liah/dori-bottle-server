package com.liah.doribottle.service.account.dto

import com.liah.doribottle.domain.user.Role
import java.util.UUID

data class AdminDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role
)