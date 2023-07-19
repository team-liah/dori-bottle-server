package com.liah.doribottle.web.rental.vm

import jakarta.validation.constraints.NotNull
import java.util.UUID

data class RentRequest(
    @field:NotNull
    val machineId: UUID?, //TODO: machineNo or machineId
    @field:NotNull
    val cupRfid: String?,
    @field:NotNull
    val withIce: Boolean?
)