package com.liah.doribottle.service.notification

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.notification.Alert
import com.liah.doribottle.domain.notification.Notification
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.notification.NotificationQueryRepository
import com.liah.doribottle.repository.notification.NotificationRepository
import com.liah.doribottle.service.notification.dto.NotificationDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationQueryRepository: NotificationQueryRepository,
    private val alertRepository: AlertRepository
) {
    @Transactional
    fun save(
        userId: UUID,
        type: NotificationType,
        title: String,
        content: String,
        targetId: UUID?
    ): UUID {
        val notification = notificationRepository.save(
            Notification(
                userId = userId,
                type = type,
                title = title,
                content = content,
                targetId = targetId
            )
        )

        return notification.id
    }

    @Transactional(readOnly = true)
    fun getAll(
        userId: UUID,
        pageable: Pageable
    ): Page<NotificationDto> {
        return notificationQueryRepository.getAll(
            userId = userId,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional
    fun read(
        id: UUID
    ) {
        val notification = notificationRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUNT)

        notification.read()
    }

    fun alert(userId: UUID) {
        val userIdString = userId.toString()
        val alert = alertRepository.findByIdOrNull(userIdString)
            ?: Alert(userId = userIdString)
        alert.increaseCount()

        alertRepository.save(alert)
    }

    fun clearAlert(userId: UUID) {
        alertRepository.deleteById(userId.toString())
    }

    fun getAlertCount(userId: UUID) = alertRepository.findByIdOrNull(userId.toString())?.count ?: 0
}