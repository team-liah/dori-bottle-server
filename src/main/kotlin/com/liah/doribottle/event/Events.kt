package com.liah.doribottle.event

import com.liah.doribottle.domain.notification.NotificationIndividual
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
            individual: NotificationIndividual
        ) {
            notifyAll(listOf(individual))
        }

        fun notifyAll(
            individuals: List<NotificationIndividual>
        ) {
            publisher.publishEvent(NotificationSaveEvent(individuals))
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