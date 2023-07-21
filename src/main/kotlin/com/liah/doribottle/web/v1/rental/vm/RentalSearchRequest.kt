package com.liah.doribottle.web.v1.rental.vm

import com.liah.doribottle.domain.rental.RentalStatus

data class RentalSearchRequest(
    val status: RentalStatus? = null
)