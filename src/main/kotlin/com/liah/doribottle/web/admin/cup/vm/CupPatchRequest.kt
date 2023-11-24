package com.liah.doribottle.web.admin.cup.vm

import com.liah.doribottle.domain.cup.CupStatus

data class CupPatchRequest(
    val rfid: String?,
    val status: CupStatus?
)