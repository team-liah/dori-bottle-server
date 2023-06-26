package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.*
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
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

    fun authRequest(loginPassword: String) {
        this.loginPassword = loginPassword
        this.loginExpirationDate = Instant.now().plus(5, ChronoUnit.MINUTES)
    }

    fun auth(loginPassword: String) {
        if (this.loginExpirationDate == null
            || this.loginExpirationDate!! < Instant.now()) throw BadCredentialsException("인증시간이 초과되었습니다.")
        if (this.loginPassword != loginPassword) throw BadCredentialsException("잘못된 인증번호입니다.")
        if (!this.active) throw DisabledException("비활성화된 계정입니다.")
        if (this.blocked) throw LockedException("정지된 계정입니다.")

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
}