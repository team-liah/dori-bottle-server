package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.web.v1.machine.vm.MachineResponse
import java.time.Instant
import java.util.*

data class MachineDto(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto,
    val location: LocationDto,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState,
    val imageUrl: String?,
    val rentCupAmounts: Long?,
    val rentIceCupAmounts: Long?,
    val createdDate: Instant,
    val lastModifiedDate: Instant,
) {
    fun toResponse() =
        MachineResponse(id, no, name, type, address, location, capacity, cupAmounts, state, imageUrl, rentCupAmounts, rentIceCupAmounts)
}
