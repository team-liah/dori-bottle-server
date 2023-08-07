package com.liah.doribottle.web.v1.rental.vm

import jakarta.validation.constraints.NotNull

data class UpdateRentalCupRequest(
    @field:NotNull
    val cupRfid: String?
)