package com.liah.doribottle.web.v1.inquiry.vm

import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.service.inquiry.dto.BankAccountDto
import com.liah.doribottle.service.inquiry.dto.InquiryTargetDto
import jakarta.validation.constraints.NotNull

data class InquiryRegisterRequest(
    @field:NotNull
    val type: InquiryType?,
    val bankAccountDto: BankAccountDto?,
    val content: String?,
    val target: InquiryTargetDto?,
    val imageUrls: List<String>?,
)
