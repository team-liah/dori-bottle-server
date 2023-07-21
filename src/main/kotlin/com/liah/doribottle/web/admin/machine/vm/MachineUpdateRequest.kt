package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.service.common.AddressDto
import jakarta.validation.constraints.NotNull

data class MachineUpdateRequest(
    @field:NotNull
    val name: String?,
    @field:NotNull
    val address: AddressDto?,
    @field:NotNull
    val capacity: Int?,
    @field:NotNull
    val cupAmounts: Int?
)