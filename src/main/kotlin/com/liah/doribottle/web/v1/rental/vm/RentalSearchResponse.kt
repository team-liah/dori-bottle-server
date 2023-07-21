package com.liah.doribottle.web.v1.rental.vm

import com.liah.doribottle.domain.rental.RentalStatus
import java.time.Instant
import java.util.UUID

data class RentalSearchResponse(
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