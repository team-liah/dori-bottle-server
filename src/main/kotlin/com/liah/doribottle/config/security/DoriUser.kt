package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import java.util.*

data class DoriUser(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role,
    val groupCode: String?
) : User(id.toString(), "", groupCode?.let { mutableListOf(SimpleGrantedAuthority(role.key), SimpleGrantedAuthority(it)) } ?: mutableListOf(SimpleGrantedAuthority(role.key)))