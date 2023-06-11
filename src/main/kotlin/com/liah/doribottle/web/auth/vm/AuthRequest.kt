package com.liah.doribottle.web.auth.vm

import jakarta.validation.constraints.NotEmpty

data class AuthRequest(
    @field:NotEmpty
    val loginId: String?,
    @field:NotEmpty
    val password: String?
)