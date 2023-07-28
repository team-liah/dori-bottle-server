package com.liah.doribottle.event.notification

import com.liah.doribottle.domain.notification.NotificationType
import java.util.*

data class NotificationSaveEvent(
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val content: String,
    val targetId: UUID?
)