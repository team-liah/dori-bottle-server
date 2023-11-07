package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.service.common.AddressDto
import jakarta.validation.constraints.Min

data class MachinePatchRequest(
    val name: String?,
    val address: AddressDto?,
    @field:Min(0)
    val capacity: Int?,
    @field:Min(0)
    val cupAmounts: Int?,
    val state: MachineState?
)