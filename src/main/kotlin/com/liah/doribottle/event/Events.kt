package com.liah.doribottle.event

import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.event.notification.NotificationSaveEvent
import com.liah.doribottle.event.user.FirstRentalUseEvent
import org.springframework.context.ApplicationEventPublisher
import java.util.*

class Events {
    companion object {
        private lateinit var publisher: ApplicationEventPublisher

        fun setPublisher(publisher: ApplicationEventPublisher) {
            this.publisher = publisher
        }

        fun notify(
            userId: UUID,
            type: NotificationType,
            content: String,
            targetId: UUID?
        ) {
            publisher.publishEvent(
                NotificationSaveEvent(
                    userId = userId,
                    type = type,
                    title = type.title,
                    content = content,
                    targetId = targetId
                )
            )
        }

        fun useFirstRental(
            userId: UUID
        ) {
            publisher.publishEvent(
                FirstRentalUseEvent(
                    userId = userId
                )
            )
        }
    }
}