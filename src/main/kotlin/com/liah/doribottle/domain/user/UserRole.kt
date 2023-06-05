package com.liah.doribottle.domain.user

enum class UserRole(
    val title: String
) {
    ROLE_USER("일반 유저"),
    ROLE_INSTITUTION("기관")
}