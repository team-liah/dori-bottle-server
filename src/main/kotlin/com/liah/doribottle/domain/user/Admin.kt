package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "admin",
    indexes = [Index(name = "IDX_ADMIN_LOGIN_ID", columnList = "loginId")]
)
class Admin(
    loginId: String,
    loginPassword: String,
    name: String,
    role: Role
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        protected set

    @Column(nullable = false)
    var loginPassword: String = loginPassword
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role =
        if (role != Role.GUEST && role != Role.USER) role
        else throw IllegalArgumentException("Non Admin role is not allowed.")
}