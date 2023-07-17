package com.liah.doribottle.web.account.vm

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String?
)