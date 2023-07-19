package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.common.Address
import jakarta.validation.constraints.NotNull

data class MachineUpdateRequest(
    @field:NotNull
    val address: Address?,
    @field:NotNull
    val capacity: Int?,
    @field:NotNull
    val cupAmounts: Int?
)