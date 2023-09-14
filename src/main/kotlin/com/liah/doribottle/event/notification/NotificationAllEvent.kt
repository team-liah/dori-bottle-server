package com.liah.doribottle.event.notification

import com.liah.doribottle.domain.notification.NotificationType
import java.util.*

class NotificationAllEvent(
    val type: NotificationType,
    val targetId: UUID?
)