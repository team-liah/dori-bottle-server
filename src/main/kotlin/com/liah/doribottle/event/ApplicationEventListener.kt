package com.liah.doribottle.event

import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.event.dummy.DummyInitEvent
import com.liah.doribottle.event.notification.NotificationAllEvent
import com.liah.doribottle.event.notification.NotificationIndividualEvent
import com.liah.doribottle.event.user.FirstRentalUseEvent
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.cup.CupService
import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.service.user.AdminService
import com.liah.doribottle.service.user.UserService
import org.springframework.context.event.EventListener
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val notificationService: NotificationService,
    private val accountService: AccountService,
    private val adminService: AdminService,
    private val machineService: MachineService,
    private val cupService: CupService,
    private val userService: UserService
) {
    @Async
    @TransactionalEventListener(NotificationIndividualEvent::class)
    fun handleNotificationIndividualEvent(event: NotificationIndividualEvent) {
        notificationService.saveAll(event.individuals)
        event.individuals.forEach { notificationService.alert(it.userId) }
    }

    @Async
    @TransactionalEventListener(NotificationAllEvent::class)
    fun handleNotificationAllEvent(event: NotificationAllEvent) {
        val users = userService.getAll(
            active = true,
            pageable = Pageable.unpaged()
        )
        val individuals = users.content.map { user ->
            NotificationIndividual(
                userId = user.id,
                type = event.type,
                targetId = event.targetId
            )
        }
        notificationService.saveAll(individuals)
        individuals.forEach { notificationService.alert(it.userId) }
    }

    @Async
    @TransactionalEventListener(FirstRentalUseEvent::class)
    fun handleFirstRentalUseEvent(event: FirstRentalUseEvent) {
        userService.rewardInviterByInvitee(event.userId)
    }

    // TODO: Remove
    @EventListener(DummyInitEvent::class)
    fun handleContextRefreshedEvent(event: DummyInitEvent) {
        adminService.createDummyAdmin("admin", "qwer1234", "machine", "qwer1234")
        accountService.createDummyUser("010-7777-7777")
        machineService.createDummyMachine("111-1111", "222-2222")
        cupService.createDummyCup(listOf("00 00 00 00","11 11 11 11", "22 22 22 22", "33 33 33 33", "44 44 44 44", "55 55 55 55", "66 66 66 66", "77 77 77 77", "88 88 88 88", "99 99 99 99"))
    }
}