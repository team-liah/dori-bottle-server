package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import java.util.*

data class MachineDto(
    val id: UUID,
    val no: String,
    val type: MachineType,
    val address: Address,
    val capacity: Long,
    val cupAmounts: Int,
    val state: MachineState
)