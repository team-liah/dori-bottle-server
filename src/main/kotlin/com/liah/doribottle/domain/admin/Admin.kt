package com.liah.doribottle.domain.admin

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "admin",
    indexes = [Index(name = "IDX_ADMIN_LOGIN_ID", columnList = "loginId")]
)
class Admin(
    loginId: String,
    email: String,
    role: AdminRole
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        private set

    var loginPassword: String? = null
        private set

    @Column(nullable = false)
    var name: String = generateTempName()
        private set

    @Column(nullable = false)
    var email: String = email
        private set

    @Column
    var phoneNumber: String? = null
        private set

    @Column
    val activeKey: UUID? = UUID.randomUUID()

    @Column(nullable = false)
    var active: Boolean = false
        private set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: AdminRole = role

    private fun generateTempName(): String {
        val charPool = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val randomString = List(7) { charPool.random() }.joinToString("")
        return "ADMIN-$randomString"
    }
}