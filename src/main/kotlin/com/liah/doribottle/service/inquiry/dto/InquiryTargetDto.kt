package com.liah.doribottle.service.inquiry.dto

import com.liah.doribottle.domain.inquiry.InquiryTarget
import java.util.UUID

data class InquiryTargetDto(
    val id: UUID,
    val classType: String,
) {
    fun toEmbeddable() = InquiryTarget(id, classType)
}
