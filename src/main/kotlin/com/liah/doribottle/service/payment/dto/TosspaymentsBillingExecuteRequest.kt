package com.liah.doribottle.service.payment.dto

data class TosspaymentsBillingExecuteRequest(
    val customerKey: String,
    val amount: Long,
    val orderId: String,
    val orderName: String
)