package com.liah.doribottle.service.sms.dto

data class NcloudSmsSendRequest(
    val type: String = "SMS",
    val contentType: String = "COMM",
    val countryCode: String = "82",
    val from: String,
    val messages: List<Message>
) {
    data class Message(
        val to: String,
        val content: String
    )

    constructor(
        from: String,
        to: String,
        content: String
    ): this(
        from = from,
        messages = listOf(Message(to, content))
    )
}