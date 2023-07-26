package com.liah.doribottle.web.v1.account.vm

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)