package com.liah.doribottle.extension

import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.constant.AuthorityConstant
import com.liah.doribottle.domain.user.Admin
import org.springframework.security.core.context.SecurityContextHolder
import java.util.UUID

fun currentUser() = getPrincipal()

fun currentUserId() = getPrincipal()?.id

fun currentUserLoginId() = getPrincipal()?.loginId

private fun getPrincipal(): DoriUser? {
    val principal = SecurityContextHolder.getContext().authentication?.principal
    return if (principal is DoriUser?) {
        principal
    } else {
        null
    }
}

fun systemId(): UUID = UUID.fromString(AuthorityConstant.SYSTEM_ID)

fun Admin.isSystem(): Boolean = this.id == UUID.fromString(AuthorityConstant.SYSTEM_ID)
