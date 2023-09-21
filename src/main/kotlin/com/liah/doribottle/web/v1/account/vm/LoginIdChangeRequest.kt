package com.liah.doribottle.web.v1.account.vm

import jakarta.validation.constraints.NotEmpty

data class LoginIdChangeRequest(
    @field:NotEmpty
    val authCode: String?
)