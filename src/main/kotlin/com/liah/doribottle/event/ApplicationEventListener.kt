package com.liah.doribottle.event

import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.service.point.PointService
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val pointService: PointService
) {
    @Async
    @TransactionalEventListener(PointSaveEvent::class)
    fun handlePointSaveEvent(event: PointSaveEvent) {
        pointService.save(event.userId, event.saveType, event.historyType, event.saveAmounts, event.description)
    }
}