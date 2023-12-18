package com.liah.doribottle.service.machine.dto

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.service.group.dto.GroupDto
import com.liah.doribottle.service.user.dto.UserSimpleDto
import java.time.Instant
import java.util.*

data class MachineDetailDto(
    val id: UUID,
    val no: String,
    val name: String,
    val type: MachineType,
    val address: AddressDto,
    val location: LocationDto,
    val capacity: Int,
    val cupAmounts: Int,
    val state: MachineState,
    val managers: List<UserSimpleDto>,
    val managementGroups: List<GroupDto>,
    val deleted: Boolean,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)