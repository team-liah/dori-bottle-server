package com.liah.doribottle.service.payment.dto

data class BillingKeyIssueRequest(
    val authKey: String,
    val customerKey: String,
) : TossPaymentsRestApiRequest()