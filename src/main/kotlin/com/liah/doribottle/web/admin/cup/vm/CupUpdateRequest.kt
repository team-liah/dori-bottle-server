package com.liah.doribottle.web.admin.cup.vm

import com.liah.doribottle.domain.cup.CupStatus
import jakarta.validation.constraints.NotNull

data class CupUpdateRequest(
    @field:NotNull
    val rfid: String?,
    @field:NotNull
    val status: CupStatus?
)