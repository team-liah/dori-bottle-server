package com.liah.doribottle.apiclient.vm

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class NcloudSmsSendRequest(
    val type: String = "SMS",
    val contentType: String = "COMM",
    val countryCode: String = "82",
    val from: String,
    val content: String,
    val messages: List<Message>,
) {
    data class Message(
        val to: String,
        val content: String? = null,
    )

    constructor(
        from: String,
        to: String,
        content: String,
    ) : this(
        from = from,
        content = content,
        messages = listOf(Message(to)),
    )
}
