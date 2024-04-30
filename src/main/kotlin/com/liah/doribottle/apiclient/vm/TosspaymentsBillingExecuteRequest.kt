package com.liah.doribottle.apiclient.vm

data class TosspaymentsBillingExecuteRequest(
    val customerKey: String,
    val amount: Long,
    val orderId: String,
    val orderName: String,
)
