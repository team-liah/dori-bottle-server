package com.liah.doribottle.web.v1.inquiry.vm

import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import java.time.Instant
import java.util.*

data class InquirySearchResponse(
    val id: UUID,
    val type: InquiryType,
    val bankAccount: BankAccountDto?,
    val content: String?,
    val answer: String?,
    val status: InquiryStatus,
    val createdDate: Instant
)