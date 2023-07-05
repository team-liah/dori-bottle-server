package com.liah.doribottle.config.security

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContextFactory
import java.util.*

class WithMockDoriUserSecurityContextFactory : WithSecurityContextFactory<WithMockDoriUser> {
    override fun createSecurityContext(annotation: WithMockDoriUser): SecurityContext {
        val context = SecurityContextHolder.createEmptyContext()
        val principal = DoriUser(UUID.randomUUID(), annotation.loginId, annotation.role)

        val authenticationToken = UsernamePasswordAuthenticationToken(
            principal,
            null,
            listOf(SimpleGrantedAuthority(annotation.role.key))
        )

        context.authentication = authenticationToken

        return context
    }
}