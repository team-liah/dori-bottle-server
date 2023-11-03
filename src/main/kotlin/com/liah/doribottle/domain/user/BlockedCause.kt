package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.user.dto.BlockedCauseDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(
    name = "blocked_cause",
    indexes = [Index(name = "INDEX_BLOCKED_CAUSE_USER_ID", columnList = "user_id")]
)
class BlockedCause(
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: BlockedCauseType,

    @Column
    val description: String? = null
) : PrimaryKeyEntity() {
    fun toDto() = BlockedCauseDto(id, user.id, type, description, createdDate, lastModifiedDate)
}