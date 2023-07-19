package com.liah.doribottle.service.account.dto

import com.liah.doribottle.web.v1.account.vm.AuthResponse

data class AuthDto(
    val accessToken: String,
    val refreshToken: String?
) {
    fun toResponse() = AuthResponse(accessToken, refreshToken)
    fun toAdminResponse() = com.liah.doribottle.web.admin.account.vm.AuthResponse(accessToken)
}