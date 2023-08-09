package com.liah.doribottle.web.v1.me.vm

import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.domain.user.Role
import java.util.*

data class MeResponse(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role,
    val alertCount: Int
) {
    companion object {
        fun of(
            doriUser: DoriUser,
            alertCount: Int
        ) = MeResponse(
            id = doriUser.id,
            loginId = doriUser.loginId,
            name = doriUser.name,
            role = doriUser.role,
            alertCount = alertCount
        )
    }
}