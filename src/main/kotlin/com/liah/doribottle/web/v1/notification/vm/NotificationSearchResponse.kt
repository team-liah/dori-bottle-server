package com.liah.doribottle.web.v1.notification.vm

import com.liah.doribottle.domain.notification.NotificationType
import java.util.*

data class NotificationSearchResponse(
    val id: UUID,
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val content: String,
    val targetId: UUID?,
    val read: Boolean
)