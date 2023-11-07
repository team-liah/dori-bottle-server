package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.service.common.AddressDto
import jakarta.validation.constraints.Min

data class MachinePatchUpdateRequest(
    val name: String?,
    val address: AddressDto?,
    @field:Min(0)
    val capacity: Int?,
    @field:Min(0)
    val cupAmounts: Int?
)