package com.liah.doribottle.web.admin.machine.vm

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class MachineCupAmountsUpdateRequest(
    @field:Min(0)
    @field:NotNull
    val cupAmounts: Int?
)