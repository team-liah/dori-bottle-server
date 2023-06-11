package com.liah.doribottle.web.auth.vm

import jakarta.validation.constraints.NotEmpty

data class UserJoinRequest(
    @field:NotEmpty
    val loginId: String?,
    @field:NotEmpty
    val name: String?
)