package com.liah.doribottle.web.admin.rental.vm

import jakarta.validation.constraints.NotNull

data class ReturnRequest(
    @field:NotNull
    val machineNo: String?,
    @field:NotNull
    val cupRfid: String?
)