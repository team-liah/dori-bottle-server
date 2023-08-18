package com.liah.doribottle.web.admin.rental.vm

import jakarta.validation.constraints.NotNull

data class RentalCupUpdateRequest(
    @field:NotNull
    val cupRfid: String?
)