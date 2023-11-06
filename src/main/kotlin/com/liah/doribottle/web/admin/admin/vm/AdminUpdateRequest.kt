package com.liah.doribottle.web.admin.admin.vm

import com.liah.doribottle.domain.user.Role
import jakarta.validation.constraints.NotNull

data class AdminUpdateRequest(
    @field:NotNull
    val loginId: String?,
    @field:NotNull
    val name: String?,
    @field:NotNull
    val role: Role?,
    val email: String?,
    val phoneNumber: String?,
    val description: String?
)