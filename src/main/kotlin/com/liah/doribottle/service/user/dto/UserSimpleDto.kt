package com.liah.doribottle.service.user.dto

import java.util.*

data class UserSimpleDto(
    val id: UUID,
    val loginId: String,
    val name: String,
    val phoneNumber: String
)