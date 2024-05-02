package com.liah.doribottle.event.notification

import com.liah.doribottle.apiclient.vm.SlackMessageType

data class NotificationSlackMessageEvent(
    val type: SlackMessageType,
    val body: Any? = null,
)
