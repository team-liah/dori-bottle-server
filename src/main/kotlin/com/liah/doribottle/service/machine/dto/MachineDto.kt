package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.web.admin.machine.vm.MachineSearchResponse
import java.util.*

data class MachineDto(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: Address,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState
) {
    fun toSearchResponse() = MachineSearchResponse(id, no, name, type, address, capacity, cupAmounts, state)
}