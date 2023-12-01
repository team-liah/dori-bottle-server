package com.liah.doribottle.web.admin.rental.vm

import com.liah.doribottle.domain.rental.RentalStatus
import java.util.*

data class RentalSearchRequest(
    val no: String?,
    val userId: UUID?,
    val cupId: UUID?,
    val fromMachineId: UUID?,
    val toMachineId: UUID?,
    val status: RentalStatus?,
    val expired: Boolean?
)