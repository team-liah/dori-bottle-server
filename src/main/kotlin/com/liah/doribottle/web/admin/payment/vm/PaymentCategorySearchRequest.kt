package com.liah.doribottle.web.admin.payment.vm

data class PaymentCategorySearchRequest(
    val deleted: Boolean?,
    val expired: Boolean?
)