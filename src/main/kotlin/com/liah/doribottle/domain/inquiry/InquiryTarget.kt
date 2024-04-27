package com.liah.doribottle.domain.inquiry

import com.liah.doribottle.service.inquiry.dto.InquiryTargetDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class InquiryTarget(
    @Column(name = "target_id")
    val id: Long,
    @Column(name = "target_class_type")
    val classType: String,
) {
    fun toDto() = InquiryTargetDto(id, classType)
}
