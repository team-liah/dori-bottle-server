package com.liah.doribottle.web.v1.payment.vm

import org.jetbrains.annotations.NotNull
import java.util.UUID

data class PayToSavePointRequest(
    @field:NotNull
    val categoryId: UUID?
)