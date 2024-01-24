package com.liah.doribottle.domain.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.extension.isSystem
import com.liah.doribottle.service.user.dto.AdminDto
import com.liah.doribottle.service.user.dto.AdminSimpleDto
import jakarta.persistence.*
import java.util.*

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
    val role: Role = role.validateAdmin()

    @Column
    var email: String? = email
        protected set

    @Column
    var phoneNumber: String? = phoneNumber
        protected set

    @Column
    var description: String? = description
        protected set

    override fun delete() {
        if (this.isSystem())
            throw BusinessException(ErrorCode.SYSTEM_DELETE_NOT_ALLOWED)

        this.loginId = "Deleted ${UUID.randomUUID()}"
        super.delete()
    }

    fun update(
        loginId: String,
        name: String,
        email: String?,
        phoneNumber: String?,
        description: String?
    ) {
        this.loginId = loginId
        this.name = name
        this.email = email
        this.phoneNumber = phoneNumber
        this.description = description
    }

    fun updatePassword(
        loginPassword: String
    ) {
        this.loginPassword = loginPassword
    }

    fun toDto() = AdminDto(id, loginId, name, role, email, phoneNumber, description, deleted, createdDate, lastModifiedDate)
    fun toSimpleDto() = AdminSimpleDto(id, loginId, name, role)
}