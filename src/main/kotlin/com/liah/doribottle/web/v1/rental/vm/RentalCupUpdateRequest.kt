package com.liah.doribottle.web.v1.rental.vm

import jakarta.validation.constraints.NotNull

data class RentalCupUpdateRequest(
    @field:NotNull
    val cupRfid: String?
)