package com.liah.doribottle.web.admin.inquiry.vm

import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import java.util.*

data class InquirySearchRequest(
    val userId: UUID?,
    val type: InquiryType?,
    val status: InquiryStatus?,
    val keyword: String?
)