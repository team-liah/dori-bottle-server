package com.liah.doribottle.web.v1.me.vm

import com.liah.doribottle.constant.BIRTH_DATE_REGEX
import com.liah.doribottle.domain.user.Gender
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class ProfileUpdateRequest(
    @field:NotEmpty
    val name: String?,
    val gender: Gender?,
    @field:NotNull
    @field:Pattern(regexp = BIRTH_DATE_REGEX)
    val birthDate: String?
)