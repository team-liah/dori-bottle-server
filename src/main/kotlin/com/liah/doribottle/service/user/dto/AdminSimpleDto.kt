package com.liah.doribottle.service.user.dto

import com.liah.doribottle.domain.user.Role
import java.util.*

data class AdminSimpleDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role
)