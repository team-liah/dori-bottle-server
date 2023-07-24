package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import java.util.*

data class DoriUser(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role
)