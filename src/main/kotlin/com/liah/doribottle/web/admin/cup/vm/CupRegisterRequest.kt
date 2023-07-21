package com.liah.doribottle.web.admin.cup.vm

import jakarta.validation.constraints.NotNull

data class CupRegisterRequest(
    @field:NotNull
    val rfid: String?
)
