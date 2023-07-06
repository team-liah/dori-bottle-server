package com.liah.doribottle.web.account.vm

import com.liah.doribottle.constant.PHONE_NUMBER_REGEX
import com.liah.doribottle.domain.user.Gender
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class RegisterRequest(
    @field:NotEmpty
    @field:Pattern(regexp = PHONE_NUMBER_REGEX, message = "휴대전화번호 형식이 아닙니다.")
    val phoneNumber: String?,
    @field:NotEmpty
    val name: String?,
    @field:NotNull
    val gender: Gender?,
    @field:NotNull
    val birthDate: Int?,
    @field:NotNull
    val agreedTermsOfService: Boolean?,
    @field:NotNull
    val agreedTermsOfPrivacy: Boolean?,
    @field:NotNull
    val agreedTermsOfMarketing: Boolean?
)