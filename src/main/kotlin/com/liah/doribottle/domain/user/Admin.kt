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
    role: Role,
    email: String?,
    phoneNumber: String?,
    description: String?
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

    @Column
    var email: String? = email
        protected set

    @Column
    var phoneNumber: String? = phoneNumber
        protected set

    @Column
    var description: String? = description
        protected set

    fun update(
        loginId: String,
        name: String,
        role: Role,
        email: String?,
        phoneNumber: String?,
        description: String?
    ) {
        this.loginId = loginId
        this.name = name
        this.role = role
        this.email = email
        this.phoneNumber = phoneNumber
        this.description = description
    }

    fun toDto() = AdminDto(id, loginId, name, role, email, phoneNumber, description, deleted, createdDate, lastModifiedDate)
    fun toSimpleDto() = AdminSimpleDto(id, loginId, name, role)
}