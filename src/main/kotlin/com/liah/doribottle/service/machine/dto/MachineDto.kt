package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import java.time.Instant
import java.util.*

data class MachineDto(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto?,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)