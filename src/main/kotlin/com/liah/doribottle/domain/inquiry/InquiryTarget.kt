package com.liah.doribottle.domain.inquiry

import com.liah.doribottle.service.inquiry.dto.InquiryTargetDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.UUID

@Embeddable
data class InquiryTarget(
    @Column(name = "target_id", columnDefinition = "BINARY(16)")
    val id: UUID,
    @Column(name = "target_class_type")
    val classType: String,
) {
    fun toDto() = InquiryTargetDto(id, classType)
}
