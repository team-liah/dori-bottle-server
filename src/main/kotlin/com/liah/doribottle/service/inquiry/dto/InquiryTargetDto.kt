package com.liah.doribottle.service.inquiry.dto

import com.liah.doribottle.domain.inquiry.InquiryTarget

data class InquiryTargetDto(
    val id: Long,
    val classType: String,
) {
    fun toEmbeddable() = InquiryTarget(id, classType)
}
