package com.liah.doribottle.web.admin.user.vm

import com.liah.doribottle.domain.user.PenaltyType
import org.jetbrains.annotations.NotNull

data class UserPenaltyImposeRequest(
    @field:NotNull
    val penaltyType: PenaltyType?,
    val penaltyCause: String?
)