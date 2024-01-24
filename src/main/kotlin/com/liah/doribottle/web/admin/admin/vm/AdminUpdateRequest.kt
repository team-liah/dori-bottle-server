package com.liah.doribottle.web.admin.admin.vm

import jakarta.validation.constraints.NotNull

data class AdminUpdateRequest(
    @field:NotNull
    val loginId: String?,
    @field:NotNull
    val name: String?,
    val email: String?,
    val phoneNumber: String?,
    val description: String?
)