package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.service.point.dto.PointDto
import com.liah.doribottle.web.v1.payment.vm.PaymentSearchResponse
import java.util.*

data class PaymentDto(
    val id: UUID,
    val userId: UUID,
    val price: Long,
    val type: PaymentType,
    val card: CardDto,
    val status: PaymentStatus,
    val result: PaymentResultDto?,
    val point: PointDto?
) {
    fun toSearchResponse() = PaymentSearchResponse(id, userId, price, type, card.toResponse(), status, point?.saveAmounts, point?.remainAmounts)
}