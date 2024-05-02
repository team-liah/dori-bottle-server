package com.liah.doribottle.event

import com.liah.doribottle.apiclient.vm.SlackMessageType
import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.event.notification.NotificationAllEvent
import com.liah.doribottle.event.notification.NotificationIndividualEvent
import com.liah.doribottle.event.notification.NotificationSlackMessageEvent
import org.springframework.context.ApplicationEventPublisher
import java.util.UUID

class Events {
    companion object {
        private lateinit var publisher: ApplicationEventPublisher

        fun setPublisher(publisher: ApplicationEventPublisher) {
            this.publisher = publisher
        }

        fun notify(individual: NotificationIndividual) {
            notifyAll(listOf(individual))
        }

        fun notifyAll(individuals: List<NotificationIndividual>) {
            publisher.publishEvent(NotificationIndividualEvent(individuals))
        }

        fun notifyAll(
            type: NotificationType,
            targetId: UUID?,
        ) {
            publisher.publishEvent(NotificationAllEvent(type, targetId))
        }

        fun notifySlack(
            type: SlackMessageType,
            body: Any? = null,
        ) {
            publisher.publishEvent(NotificationSlackMessageEvent(type, body))
        }
    }
}
