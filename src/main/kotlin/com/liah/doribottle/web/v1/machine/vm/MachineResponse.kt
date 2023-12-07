package com.liah.doribottle.web.v1.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import java.util.*

data class MachineResponse(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto,
    val location: LocationDto,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState
)
