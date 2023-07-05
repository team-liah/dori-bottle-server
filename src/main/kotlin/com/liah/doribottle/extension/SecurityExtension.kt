package com.liah.doribottle.extension

import com.liah.doribottle.config.security.DoriUser
import org.springframework.security.core.context.SecurityContextHolder

fun currentUser() = getPrincipal()

fun currentUserId() = getPrincipal()?.id

fun currentUserLoginId() = getPrincipal()?.loginId

private fun getPrincipal(): DoriUser? {
    val principal = SecurityContextHolder.getContext().authentication.principal
    return if (principal is DoriUser?) {
        principal
    } else {
        null
    }
}