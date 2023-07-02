package com.liah.doribottle.service.account.dto

import com.liah.doribottle.web.account.vm.AuthResponse

data class AuthDto(
    val accessToken: String,
    val refreshToken: String
) {
    fun toResponse() = AuthResponse(accessToken, refreshToken)
}