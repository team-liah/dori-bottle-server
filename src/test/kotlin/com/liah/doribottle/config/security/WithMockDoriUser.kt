package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import org.springframework.security.test.context.support.WithSecurityContext

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockDoriUserSecurityContextFactory::class)
annotation class WithMockDoriUser(
    val loginId: String = "010-0000-0000",
    val role: Role = Role.USER
)