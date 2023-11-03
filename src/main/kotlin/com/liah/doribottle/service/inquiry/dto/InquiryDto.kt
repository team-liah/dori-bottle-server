package com.liah.doribottle.service.inquiry.dto

import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import java.time.Instant
import java.util.*

data class InquiryDto(
    val id: UUID,
    val userId: UUID,
    val type: InquiryType,
    val bankAccount: BankAccountDto?,
    val content: String?,
    val answer: String?,
    val status: InquiryStatus,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)