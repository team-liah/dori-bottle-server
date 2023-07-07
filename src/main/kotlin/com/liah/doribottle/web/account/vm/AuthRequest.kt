package com.liah.doribottle.web.account.vm

import com.liah.doribottle.constant.PHONE_NUMBER_REGEX
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class AuthRequest(
    @field:NotEmpty
    @field:Pattern(regexp = PHONE_NUMBER_REGEX)
    val loginId: String?,
    @field:NotEmpty
    val loginPassword: String?
)