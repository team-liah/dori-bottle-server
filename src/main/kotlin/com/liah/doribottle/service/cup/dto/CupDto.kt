package com.liah.doribottle.service.cup.dto

import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.web.admin.cup.vm.CupSearchResponse
import java.util.*

data class CupDto(
    val id: UUID,
    val rfid: String,
    val status: CupStatus
) {
    fun toSearchResponse() = CupSearchResponse(id, rfid, status)
}