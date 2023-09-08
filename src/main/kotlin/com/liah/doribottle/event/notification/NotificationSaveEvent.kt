package com.liah.doribottle.event.notification

import com.liah.doribottle.domain.notification.NotificationIndividual

data class NotificationSaveEvent(
    val individuals: List<NotificationIndividual>
)