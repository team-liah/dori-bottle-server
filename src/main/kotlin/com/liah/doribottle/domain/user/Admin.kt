package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.service.user.dto.AdminDto
import com.liah.doribottle.service.user.dto.AdminSimpleDto
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
) : SoftDeleteEntity() {
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
    var role: Role = role.validateAdmin()
        set(value) {
            field = value.validateAdmin()
        }

    fun update(
        loginId: String,
        loginPassword: String,
        name: String,
        role: Role
    ) {
        this.loginId = loginId
        this.loginPassword = loginPassword
        this.name = name
        this.role = role
    }

    fun toDto() = AdminDto(id, loginId, loginPassword, name, role, deleted, createdDate, lastModifiedDate)
    fun toSimpleDto() = AdminSimpleDto(id, loginId, name, role)
}