package com.liah.doribottle.service.payment.dto

import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.service.point.dto.PointDto
import com.liah.doribottle.service.user.dto.UserSimpleDto
import com.liah.doribottle.web.v1.payment.vm.PaymentSearchResponse
import java.time.Instant
import java.util.UUID

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
    val lastModifiedDate: Instant,
) {
    fun toSearchResponse() =
        PaymentSearchResponse(
            id = id,
            userId = user.id,
            price = price,
            type = type,
            card = card.toResponse(),
            status = status,
            savePointAmounts = point?.saveAmounts,
            remainPointAmounts = point?.remainAmounts,
            createdDate = createdDate,
        )
}
