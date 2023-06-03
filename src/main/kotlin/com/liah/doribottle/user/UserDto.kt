package com.liah.doribottle.user

data class UserDto(
    val loginId: String,
    val name: String,
    val phoneNumber: String,
    val active: Boolean,
    val role: UserRole
)