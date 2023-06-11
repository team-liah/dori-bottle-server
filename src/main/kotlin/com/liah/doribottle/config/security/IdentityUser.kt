package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import java.util.*

class IdentityUser(
    loginId: String,
    password: String?,
    authorities: Collection<GrantedAuthority>,
    val userId: UUID,
    val active: Boolean,
    val blocked: Boolean
) : org.springframework.security.core.userdetails.User(loginId, password, authorities) {
    companion object {
        fun fromUser(user: User) = IdentityUser(
            loginId = user.loginId,
            password = user.loginPassword,
            authorities = authorities(user),
            userId = user.id,
            active = user.active,
            blocked = user.blocked
        )

        private fun authorities(user: User) = listOf(
            SimpleGrantedAuthority(user.loginId),
            SimpleGrantedAuthority(user.role.name)
        )
    }
}