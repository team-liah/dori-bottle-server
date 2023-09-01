package com.liah.doribottle.domain.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.extension.randomString
import com.liah.doribottle.service.user.dto.UserDetailDto
import com.liah.doribottle.service.user.dto.UserDto
import jakarta.persistence.*
import jakarta.persistence.CascadeType.ALL
import jakarta.persistence.FetchType.LAZY
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

    @Column
    var loginPassword: String? = null
        protected set

    @Column
    var loginExpirationDate: Instant? = null
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

    @Column(nullable = false)
    var phoneNumber: String = phoneNumber
        protected set

    @Column
    var birthDate: String? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Column
    var gender: Gender? = null
        protected set

    @Column(nullable = false, unique = true)
    val invitationCode: String = randomString(6)

    @Column(nullable = false)
    var invitationCount: Int = 0
        protected set

    @Column
    var inviterId: UUID? = null
        protected set

    @Column(name = "`use`", nullable = false)
    var use: Boolean = false
        protected set

    @Column(nullable = false)
    var active: Boolean = true
        protected set

    @Column(nullable = false)
    var blocked: Boolean = false
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role =
        if (role != Role.ADMIN && role != Role.MACHINE_ADMIN) role
        else throw IllegalArgumentException("Non User role is not allowed.")

    @Column
    var agreedTermsOfServiceDate: Instant? = null
        protected set

    @Column
    var agreedTermsOfPrivacyDate: Instant? = null
        protected set

    @Column
    var agreedTermsOfMarketingDate: Instant? = null
        protected set

    @Column
    var registeredDate: Instant? = null
        protected set

    @ManyToOne(fetch = LAZY)
    @JoinColumn
    var group: Group? = null
        protected set

    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
    protected val mutablePenalties: MutableList<Penalty> = mutableListOf()
    val penalties: List<Penalty> get() = mutablePenalties

    @OneToMany(mappedBy = "user", fetch = LAZY, cascade = [ALL], orphanRemoval = true)
    protected val mutableBlockedCauses: MutableList<BlockedCause> = mutableListOf()
    val blockedCauses: List<BlockedCause> get() =  mutableBlockedCauses

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
        birthDate: String?,
        gender: Gender?
    ) {
        this.name = name
        this.birthDate = birthDate
        this.gender = gender
    }

    fun register() {
        this.role = Role.USER
        this.registeredDate = Instant.now()
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

    fun imposePenalty(
        penaltyType: PenaltyType,
        penaltyCause: String?
    ) {
        this.mutablePenalties.add(Penalty(this, penaltyType, penaltyCause))
    }

    fun updateGroup(
        group: Group?
    ) {
        this.group = group
    }

    fun setInviter(inviter: User) {
        if (this.id == inviter.id) throw BusinessException(ErrorCode.INVITER_NOT_ALLOWED)
        if (this.inviterId != null) throw BusinessException(ErrorCode.INVITER_ALREADY_REGISTERED)
        if (this.registeredDate!!.until(Instant.now(), ChronoUnit.DAYS) > 30) throw BusinessException(ErrorCode.INVITER_REGISTRATION_OVERDUE)

        this.inviterId = inviter.id
    }

    fun increaseInvitationCount() {
        this.invitationCount += 1
    }

    fun use() {
        this.use = true
    }

    fun block(
        blockedCauseType: BlockedCauseType,
        blockedCauseDescription: String?
    ) {
        this.blocked = true
        this.mutableBlockedCauses.add(BlockedCause(this, blockedCauseType, blockedCauseDescription))
    }

    fun unblock(
        blockedCauseIds: Set<UUID>
    ) {
        this.mutableBlockedCauses.removeAll { blockedCauseIds.contains(it.id) }

        if (this.mutableBlockedCauses.isEmpty()) {
            this.blocked = false
        }
    }

    fun toDto() = UserDto(id, loginId, name, phoneNumber, invitationCode, birthDate, gender, role, registeredDate, group?.toDto())
    fun toDetailDto() = UserDetailDto(id, loginId, name, phoneNumber, invitationCode, invitationCount, inviterId, birthDate, gender, role, registeredDate, penalties.map { it.toDto() }, group?.toDto())
}