package com.liah.doribottle.web.v1.me.vm

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

data class InvitationCodeRegisterRequest(
    @field:Size(min = 6, max = 6)
    @field:NotNull
    val invitationCode: String?
)