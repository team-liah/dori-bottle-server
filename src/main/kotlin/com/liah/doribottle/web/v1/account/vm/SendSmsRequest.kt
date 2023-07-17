package com.liah.doribottle.web.v1.account.vm

import com.liah.doribottle.constant.PHONE_NUMBER_REGEX
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class SendSmsRequest(
    @field:NotEmpty
    @field:Pattern(regexp = PHONE_NUMBER_REGEX)
    val loginId: String?
)