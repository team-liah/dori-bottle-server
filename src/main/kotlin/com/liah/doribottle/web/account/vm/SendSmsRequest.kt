package com.liah.doribottle.web.account.vm

import jakarta.validation.constraints.NotEmpty

data class SendSmsRequest(
    @field:NotEmpty
    val loginId: String?
)