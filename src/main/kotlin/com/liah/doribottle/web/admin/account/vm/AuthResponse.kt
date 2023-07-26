package com.liah.doribottle.web.admin.account.vm

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)