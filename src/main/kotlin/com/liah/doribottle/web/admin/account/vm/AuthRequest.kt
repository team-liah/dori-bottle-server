package com.liah.doribottle.web.admin.account.vm

import jakarta.validation.constraints.NotEmpty

data class AuthRequest(
    @field:NotEmpty
    val loginId: String?,
    @field:NotEmpty
    val loginPassword: String?
)