package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.web.v1.machine.vm.MachineSimpleResponse
import com.querydsl.core.annotations.QueryProjection
import java.util.*

data class MachineSimpleDto @QueryProjection constructor(
    val id: UUID,
    val type: MachineType,
    val location: LocationDto,
    val state: MachineState
) {
    fun toResponse() = MachineSimpleResponse(id, type, location, state)
}