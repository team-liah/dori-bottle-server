package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.PrimaryKeyEntity
import com.liah.doribottle.service.dto.UserDto
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "`user`",
    indexes = [Index(name = "IDX_USER_LOGIN_ID", columnList = "loginId")]
)
class User(
    loginId: String,
    name: String,
    phoneNumber: String,
    role: UserRole
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        private set

    var loginPassword: String? = null
        private set

    @Column(nullable = false)
    var name: String = name
        private set

    @Column(nullable = false)
    var phoneNumber: String = phoneNumber
        private set

    @Column(nullable = false, unique = true)
    val invitationKey: UUID = UUID.randomUUID()

    @Column(nullable = false)
    var active: Boolean = true
        private set

    @Column(nullable = false)
    var blocked: Boolean = false
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = role

    fun toDto() = UserDto(loginId, name, phoneNumber, invitationKey, active, blocked, role)
}