package com.liah.doribottle.event.notification

import com.liah.doribottle.domain.notification.NotificationIndividual

data class NotificationIndividualEvent(
    val individuals: List<NotificationIndividual>
)