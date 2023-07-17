package com.liah.doribottle.service.account.dto

import com.liah.doribottle.domain.user.Role

data class AdminDto(
    val id: String,
    val loginId: String,
    val name: String,
    val role: Role
)