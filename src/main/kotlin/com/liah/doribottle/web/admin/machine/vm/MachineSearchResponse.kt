package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import java.util.*

data class MachineSearchResponse(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto?,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState
)