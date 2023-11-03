package com.liah.doribottle.service.notification.dto

import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.web.v1.notification.vm.NotificationSearchResponse
import java.time.Instant
import java.util.*

data class NotificationDto(
    val id: UUID,
    val userId: UUID,
    val type: NotificationType,
    val title: String,
    val content: String,
    val targetId: UUID?,
    val read: Boolean,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toSearchResponse() = NotificationSearchResponse(id, userId, type, title, content, targetId, read, createdDate)
}