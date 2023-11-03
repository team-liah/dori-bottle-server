package com.liah.doribottle.web.admin.payment.vm

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class PaymentCategoryRegisterOrUpdateRequest(
    @field:NotNull
    val amounts: Long?,
    @field:NotNull
    val price: Long?,
    @field:NotNull
    @field:Max(value = 100)
    @field:Min(value = 0)
    val discountRate: Int?,
    val discountExpiredDate: Instant?,
    val expiredDate: Instant?
)