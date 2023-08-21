package com.liah.doribottle.web.v1.payment.vm

data class CardResponse(
    val acquirer: String,
    val number: String,
    val type: String,
    val ownerType: String
)