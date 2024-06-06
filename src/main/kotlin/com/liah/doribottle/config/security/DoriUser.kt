package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.user.dto.AdminDto
import com.liah.doribottle.service.user.dto.UserDto
import java.util.UUID

data class DoriUser(
    val id: UUID,
    val loginId: String,
    val name: String,
    val role: Role,
) {
    companion object {
        fun fromUser(user: UserDto): DoriUser {
            return DoriUser(
                id = user.id,
                loginId = user.loginId,
                name = user.name,
                role = user.role,
            )
        }

        fun fromAdmin(admin: AdminDto): DoriUser {
            return DoriUser(
                id = admin.id,
                loginId = admin.loginId,
                name = admin.name,
                role = admin.role,
            )
        }
    }
}
