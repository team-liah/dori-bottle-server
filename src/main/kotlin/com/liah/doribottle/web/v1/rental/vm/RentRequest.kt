package com.liah.doribottle.web.v1.rental.vm

import jakarta.validation.constraints.NotNull

data class RentRequest(
    @field:NotNull
    val cupRfid: String?,
    @field:NotNull
    val machineNo: String?,
    @field:NotNull
    val withIce: Boolean?
)