package com.liah.doribottle.user

enum class Role(
    name: String
) {
    ROLE_ADMIN("관리자"),
    ROLE_MACHINE_ADMIN("자판기 관리자"),
    ROLE_USER("일반 유저")
}