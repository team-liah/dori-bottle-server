package com.liah.doribottle.service.payment.dto

data class TossBillingExecuteRequest(
    val customerKey: String,
    val amount: Long,
    val orderId: String,
    val orderName: String
) : TossPaymentsRestApiRequest()