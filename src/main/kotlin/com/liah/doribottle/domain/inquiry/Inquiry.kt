package com.liah.doribottle.domain.inquiry

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.inquiry.InquiryStatus.PROCEEDING
import com.liah.doribottle.domain.inquiry.InquiryStatus.SUCCEEDED
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.inquiry.dto.InquiryDto
import jakarta.persistence.*
import jakarta.persistence.FetchType.LAZY

@Entity
@Table(name = "inquiry")
class Inquiry(
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: InquiryType,

    @Embedded
    val bankAccount: BankAccount?,

    @Column
    val content: String?,
) : PrimaryKeyEntity() {
    @Column
    var answer: String? = null
        protected set

    @Column(nullable = false)
    var status: InquiryStatus = PROCEEDING
        protected set

    fun succeed(
        answer: String?
    ) {
        this.answer = answer
        this.status = SUCCEEDED
    }

    fun toDto() = InquiryDto(id, user.id, type, bankAccount?.toDto(), content, answer, status, createdDate, lastModifiedDate)
}