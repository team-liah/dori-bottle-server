package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    role: Role
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    var loginId: String = loginId
        protected set

    var loginPassword: String? = null
        protected set

    var loginExpirationDate: Instant? = null
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var phoneNumber: String = phoneNumber
        protected set

    @Column
    var birthDate: Int? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column
    var gender: Gender? = null
        protected set

    @Column(nullable = false, unique = true)
    val invitationKey: UUID = UUID.randomUUID()

    @Column(nullable = false)
    var active: Boolean = true
        protected set

    @Column(nullable = false)
    var blocked: Boolean = false
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = role

    @Column
    var agreedTermsOfServiceDate: Instant? = null
        protected set

    @Column
    var agreedTermsOfPrivacyDate: Instant? = null
        protected set

    @Column
    var agreedTermsOfMarketingDate: Instant? = null
        protected set

    fun updatePassword(loginPassword: String) {
        this.loginPassword = loginPassword
        this.loginExpirationDate = Instant.now().plus(5, ChronoUnit.MINUTES)
    }

    fun authSuccess() {
        this.loginPassword = null
        this.loginExpirationDate = null
    }

    fun update(
        name: String,
        birthDate: Int?,
        gender: Gender?
    ) {
        this.name = name
        this.birthDate = birthDate
        this.gender = gender
    }

    fun changeRole(role: Role) {
        this.role = role
    }

    fun agreeOnTerms(
        agreedTermsOfService: Boolean,
        agreedTermsOfPrivacy: Boolean,
        agreedTermsOfMarketing: Boolean
    ) {
        when (agreedTermsOfService) {
            true -> this.agreedTermsOfServiceDate = Instant.now()
            false -> this.agreedTermsOfServiceDate = null
        }
        when (agreedTermsOfPrivacy) {
            true -> this.agreedTermsOfPrivacyDate = Instant.now()
            false -> this.agreedTermsOfPrivacyDate = null
        }
        when (agreedTermsOfMarketing) {
            true -> this.agreedTermsOfMarketingDate = Instant.now()
            false -> this.agreedTermsOfMarketingDate = null
        }
    }
}