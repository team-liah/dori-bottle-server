package com.liah.doribottle.service.rental.dto

import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.web.v1.rental.vm.RentalMachineInfo
import com.liah.doribottle.web.v1.rental.vm.RentalSearchResponse
import java.time.Instant
import java.util.*

data class RentalDto(
    val id: UUID,
    val userId: UUID,
    val cupId: UUID,
    val fromMachine: MachineDto,
    val toMachine: MachineDto?,
    val withIce: Boolean,
    val cost: Long,
    val succeededDate: Instant?,
    val expiredDate: Instant,
    val status: RentalStatus,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toUserResponse() = RentalSearchResponse(
        id = id,
        userId = userId,
        cupId = cupId,
        fromMachine = RentalMachineInfo(
            id = fromMachine.id,
            no = fromMachine.no,
            name = fromMachine.name,
            type = fromMachine.type,
            address = fromMachine.address
        ),
        toMachine = toMachine?.let {
            RentalMachineInfo(
                id = it.id,
                no = it.no,
                name = it.name,
                type = it.type,
                address = it.address
            )
        },
        withIce = withIce,
        cost = cost,
        succeededDate = succeededDate,
        expiredDate = expiredDate,
        status = status,
        createdDate = createdDate
    )
}