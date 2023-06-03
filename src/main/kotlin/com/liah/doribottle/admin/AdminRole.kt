package com.liah.doribottle.admin

enum class AdminRole(
    val title: String,
    val mask: Int
) {
    ROLE_MACHINE_ADMIN("자판기 관리자", 1 shl 0),
    ROLE_ADMIN("전체 관리자", 1 shl 1)
}