package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.user.dto.PenaltyDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(
    name = "penalty",
    indexes = [Index(name = "IDX_PENALTY_USER_ID", columnList = "user_id")]
)
class Penalty(
    user: User,
    type: PenaltyType,
    cause: String?,
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = user

    @Column(nullable = false)
    var type: PenaltyType = type
        protected set

    @Column
    var cause: String? = cause
        protected set

    fun toDto() = PenaltyDto(id, user.id, type, cause)
}