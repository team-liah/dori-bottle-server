package com.liah.doribottle.service.payment.dto

data class TosspaymentsBillingKeyIssueRequest(
    val authKey: String,
    val customerKey: String,
)