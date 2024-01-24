package com.liah.doribottle.web.v1.account.vm

import com.liah.doribottle.constant.RegexConstant
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class AuthRequest(
    @field:NotEmpty
    @field:Pattern(regexp = RegexConstant.PHONE_NUMBER_REGEX)
    val loginId: String?,
    @field:NotEmpty
    val loginPassword: String?
)