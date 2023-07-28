package com.liah.doribottle.service.notification.dto

import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.web.v1.notification.vm.NotificationSearchResponse
import java.util.*

data class NotificationDto(
    val id: UUID,
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val content: String,
    val targetId: UUID?,
    val read: Boolean
) {
    fun toSearchResponse() = NotificationSearchResponse(id, userId, type, title, content, targetId, read)
}