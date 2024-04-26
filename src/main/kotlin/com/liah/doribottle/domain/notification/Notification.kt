package com.liah.doribottle.domain.notification

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.notification.dto.NotificationDto
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "notification",
    indexes = [Index(name = "INDEX_NOTIFICATION_USER_ID", columnList = "userId")]
)
class Notification(
    @Column(nullable = false)
    val userId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    @Column(nullable = false)
    val title: String,

    @Column(nullable = false)
    val content: String,

    @Column
    val targetId: UUID? = null
) : PrimaryKeyEntity() {
    @Column(name = "`read`", nullable = false)
    var read: Boolean = false
        protected set

    fun read() {
        this.read = true
    }

    fun toDto() = NotificationDto(id, userId, type, title, content, targetId, read, createdDate, lastModifiedDate)
}