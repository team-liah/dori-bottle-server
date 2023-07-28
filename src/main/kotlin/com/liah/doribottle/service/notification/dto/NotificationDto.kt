package com.liah.doribottle.service.notification.dto

import com.liah.doribottle.domain.notification.NotificationType
import java.util.*

data class NotificationDto(
    val id: UUID,
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val content: String,
    val targetId: UUID?,
    val read: Boolean
)