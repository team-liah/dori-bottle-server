package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import jakarta.validation.constraints.Min
import java.util.*

data class MachinePatchRequest(
    val name: String?,
    val address: AddressDto?,
    val location: LocationDto?,
    @field:Min(0)
    val capacity: Int?,
    @field:Min(0)
    val cupAmounts: Int?,
    val state: MachineState?,
    val managerIds: Set<UUID>?,
    val managementGroupCodes: Set<String>?
)