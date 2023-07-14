package com.liah.doribottle.service.cup.dto

import com.liah.doribottle.domain.cup.CupStatus
import java.util.*

data class CupDto(
    val id: UUID,
    val rfid: String,
    val status: CupStatus
)