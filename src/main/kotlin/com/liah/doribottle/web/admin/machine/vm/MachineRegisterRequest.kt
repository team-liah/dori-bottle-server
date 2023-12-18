package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import jakarta.validation.constraints.NotNull
import java.util.*

data class MachineRegisterRequest(
    @field:NotNull
    val no: String?,
    @field:NotNull
    val name: String?,
    @field:NotNull
    val type: MachineType?,
    @field:NotNull
    val address: AddressDto?,
    @field:NotNull
    val location: LocationDto?,
    @field:NotNull
    val capacity: Int?,
    @field:NotNull
    val managerIds: Set<UUID>?,
    @field:NotNull
    val managementGroupCodes: Set<String>?
)