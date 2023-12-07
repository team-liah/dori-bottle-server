package com.liah.doribottle.web.v1.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.LocationDto
import java.util.*

data class MachineSimpleResponse(
    val id: UUID,
    val type: MachineType,
    val location: LocationDto,
    val state: MachineState
)