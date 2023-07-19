package com.liah.doribottle.web.v1.rental.vm

import jakarta.validation.constraints.NotNull
import java.util.*

data class ReturnRequest(
    @field:NotNull
    val machineId: UUID?, //TODO: machineNo or machineId
    @field:NotNull
    val cupRfid: String?
)