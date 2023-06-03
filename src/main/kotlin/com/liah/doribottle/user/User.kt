package com.liah.doribottle.user

import com.liah.doribottle.common.jpa.PrimaryKeyEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "`user`",
    indexes = [Index(name = "IDX_USER_LOGIN_ID", columnList = "loginId")]
)
class User(
    loginId: String,
    name: String,
    phoneNumber: String,
    role: Role
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

    @Column(nullable = false)
    var active: Boolean = true
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: Role = role

    fun toDto() = UserDto(loginId, name, phoneNumber, active, role)

}