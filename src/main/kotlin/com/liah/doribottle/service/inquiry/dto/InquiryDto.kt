package com.liah.doribottle.service.inquiry.dto

import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.service.user.dto.UserSimpleDto
import com.liah.doribottle.web.v1.inquiry.vm.InquirySearchResponse
import java.time.Instant
import java.util.*

data class InquiryDto(
    val id: UUID,
    val user: UserSimpleDto,
    val type: InquiryType,
    val bankAccount: BankAccountDto?,
    val content: String?,
    val answer: String?,
    val status: InquiryStatus,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toSearchResponse(): InquirySearchResponse {
        return InquirySearchResponse(
            id = id,
            type = type,
            bankAccount = bankAccount,
            content = content,
            answer = answer,
            status = status,
            createdDate = createdDate
        )
    }
}