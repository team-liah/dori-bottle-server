package com.liah.doribottle.web.account.vm

import com.liah.doribottle.constant.PHONE_NUMBER_REGEX
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern

data class SendSmsRequest(
    @field:NotEmpty
    @field:Pattern(regexp = PHONE_NUMBER_REGEX, message = "휴대전화번호 형식이 아닙니다.")
    val loginId: String?
)