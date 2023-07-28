package com.liah.doribottle.domain.notification

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.notification.dto.NotificationDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(
    name = "notification",
    indexes = [Index(name = "INDEX_NOTIFICATION_USER_ID", columnList = "userId")]
)
class Notification(
    userId: UUID,
    type: NotificationType,
    title: String,
    content: String,
    targetId: UUID?
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val userId: UUID = userId

    @Column(nullable = false)
    val type: NotificationType = type

    @Column(nullable = false)
    val title: String = title

    @Column(nullable = false)
    val content: String = content

    @Column
    val targetId: UUID? = targetId

    @Column(name = "`read`", nullable = false)
    var read: Boolean = false
        protected set

    fun read() {
        this.read = true
    }

    fun toDto() = NotificationDto(id, userId, type, title, content, targetId, read)
}