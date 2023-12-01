package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.service.point.dto.PointDto
import com.liah.doribottle.service.user.dto.UserSimpleDto
import com.liah.doribottle.web.v1.payment.vm.PaymentSearchResponse
import java.time.Instant
import java.util.*

data class PaymentDto(
    val id: UUID,
    val user: UserSimpleDto,
    val price: Long,
    val type: PaymentType,
    val card: CardDto,
    val status: PaymentStatus,
    val result: PaymentResultDto?,
    val point: PointDto?,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toSearchResponse() = PaymentSearchResponse(id, user.id, price, type, card.toResponse(), status, point?.saveAmounts, point?.remainAmounts, createdDate)
}