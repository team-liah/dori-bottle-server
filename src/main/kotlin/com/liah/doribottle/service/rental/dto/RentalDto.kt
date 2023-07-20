package com.liah.doribottle.service.rental.dto

import com.liah.doribottle.domain.rental.RentalStatus
import java.time.Instant
import java.util.UUID

data class RentalDto(
    val id: UUID,
    val userId: UUID,
    val cupId: UUID,
    val fromMachineId: UUID,
    val toMachineId: UUID?,
    val withIce: Boolean,
    val cost: Long,
    val succeededDate: Instant?,
    val expiredDate: Instant,
    val status: RentalStatus
)