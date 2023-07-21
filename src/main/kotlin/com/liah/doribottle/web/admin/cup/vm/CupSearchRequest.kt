package com.liah.doribottle.web.admin.cup.vm

import com.liah.doribottle.domain.cup.CupStatus

data class CupSearchRequest(
    val status: CupStatus?
)