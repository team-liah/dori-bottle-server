package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.MachineType
import jakarta.validation.constraints.NotNull

data class MachineRegisterRequest(
    @field:NotNull
    val no: String?,
    @field:NotNull
    val type: MachineType?,
    @field:NotNull
    val address: Address?,
    @field:NotNull
    val capacity: Int?
)