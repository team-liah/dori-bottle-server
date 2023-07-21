package com.liah.doribottle.event

import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.service.account.AdminAccountService
import com.liah.doribottle.service.point.PointService
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val adminAccountService: AdminAccountService,
    private val pointService: PointService
) {
    @EventListener(ContextRefreshedEvent::class)
    fun handleContextRefreshedEvent(event: ContextRefreshedEvent) {
        val initLoginId = "admin"
        val initPassword = "qwer1234"
        try {
            adminAccountService.get(initLoginId)
        } catch (e: NotFoundException) {
            adminAccountService.register(
                loginId = initLoginId,
                loginPassword = initPassword,
                name = "초기 관리자",
                role = Role.ADMIN
            )
        }
    }

    @Async
    @TransactionalEventListener(PointSaveEvent::class)
    fun handlePointSaveEvent(event: PointSaveEvent) {
        pointService.save(event.userId, event.saveType, event.eventType, event.saveAmounts)
    }
}