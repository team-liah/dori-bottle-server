package com.liah.doribottle.web.account.vm

import com.liah.doribottle.domain.user.Gender
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class RegisterRequest(
    @field:NotEmpty
    val phoneNumber: String?,
    @field:NotEmpty
    val name: String?,
    @field:NotNull
    val gender: Gender?,
    @field:NotNull
    val birthDate: Int?
)