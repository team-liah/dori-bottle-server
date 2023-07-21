package com.liah.doribottle.web.v1.rental.vm

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.rental.RentalStatus
import java.time.Instant
import java.util.*

data class RentalSearchResponse(
    val id: UUID,
    val userId: UUID,
    val cupId: UUID,
    val fromMachine: RentalMachineInfo,
    val toMachine: RentalMachineInfo?,
    val withIce: Boolean,
    val cost: Long,
    val succeededDate: Instant?,
    val expiredDate: Instant,
    val status: RentalStatus
)

data class RentalMachineInfo(
    val id: UUID,
    val no: String,
    val name: String,
    val address: Address
)