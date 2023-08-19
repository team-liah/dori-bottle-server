package com.liah.doribottle.service.payment.dto

data class TossBillingKeyIssueRequest(
    val authKey: String,
    val customerKey: String,
) : TossPaymentsRestApiRequest()