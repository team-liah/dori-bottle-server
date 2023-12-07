package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class MachineUpdateRequest(
    @field:NotNull
    val name: String?,
    @field:NotNull
    val address: AddressDto?,
    @field:NotNull
    val location: LocationDto?,
    @field:Min(0)
    @field:NotNull
    val capacity: Int?,
    @field:Min(0)
    @field:NotNull
    val cupAmounts: Int?,
    @field:NotNull
    val state: MachineState?
)