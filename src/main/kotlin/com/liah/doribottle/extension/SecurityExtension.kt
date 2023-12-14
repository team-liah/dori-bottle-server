package com.liah.doribottle.extension

import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.constant.SYSTEM_ID
import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

fun currentUser() = getPrincipal()

fun currentUserId() = getPrincipal()?.id

fun currentUserLoginId() = getPrincipal()?.loginId

fun currentUserAuthorities() = getPrincipal()?.authorities?.map { it.authority } ?: emptyList()

private fun getPrincipal(): DoriUser? {
    val principal = SecurityContextHolder.getContext().authentication?.principal
    return if (principal is DoriUser?) {
        principal
    } else {
        null
    }
}

fun systemId(): UUID = UUID.fromString(SYSTEM_ID)

fun UUID.isSystem(): Boolean = this == UUID.fromString(SYSTEM_ID)