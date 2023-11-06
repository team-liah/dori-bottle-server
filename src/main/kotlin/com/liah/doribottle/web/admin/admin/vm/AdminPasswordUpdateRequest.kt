package com.liah.doribottle.web.admin.admin.vm

import jakarta.validation.constraints.NotNull

data class AdminPasswordUpdateRequest(
    @field:NotNull
    val loginPassword: String?
)