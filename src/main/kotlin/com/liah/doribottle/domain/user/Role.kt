package com.liah.doribottle.domain.user

enum class Role(
    val key: String,
    val title: String,
) {
    GUEST("ROLE_GUEST","게스트"),
    USER("ROLE_USER","일반 유저"),
    INSTITUTION("ROLE_INSTITUTION", "기관"),
    MACHINE_ADMIN("ROLE_MACHINE_ADMIN", "자판기 관리자"),
    ADMIN("ROLE_ADMIN", "관리자")
}