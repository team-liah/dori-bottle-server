package com.liah.doribottle.utils

import org.springframework.security.core.context.SecurityContextHolder
import java.util.*

fun currentUserId(): UUID? {
    val principal = SecurityContextHolder.getContext().authentication.principal
    return if (principal is UUID?) {
        principal
    } else {
        null
    }
}