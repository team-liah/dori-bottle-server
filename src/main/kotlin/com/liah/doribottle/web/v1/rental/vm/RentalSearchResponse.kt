package com.liah.doribottle.web.v1.rental.vm

import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.service.common.AddressDto
import java.time.Instant
import java.util.*

data class RentalSearchResponse(
    val id: UUID,
    val no: String,
    val userId: UUID,
    val cupId: UUID?,
    val fromMachine: RentalMachineInfo,
    val toMachine: RentalMachineInfo?,
    val withIce: Boolean,
    val cost: Long,
    val succeededDate: Instant?,
    val expiredDate: Instant,
    val status: RentalStatus,
    val createdDate: Instant
)

data class RentalMachineInfo(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto?
)