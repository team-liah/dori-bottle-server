package com.liah.doribottle.web.admin.cup.vm

import com.liah.doribottle.domain.cup.CupStatus
import java.util.*

data class CupSearchResponse(
    val id: UUID,
    val rfid: String,
    val status: CupStatus
)