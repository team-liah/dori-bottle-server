package com.liah.doribottle.event

import com.liah.doribottle.event.dummy.DummyInitEvent
import com.liah.doribottle.event.notification.NotificationSaveEvent
import com.liah.doribottle.event.user.FirstRentalUsedEvent
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.account.AdminAccountService
import com.liah.doribottle.service.cup.CupService
import com.liah.doribottle.service.machine.MachineService
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.service.user.UserService
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ApplicationEventListener(
    private val notificationService: NotificationService,
    private val accountService: AccountService,
    private val adminAccountService: AdminAccountService,
    private val machineService: MachineService,
    private val cupService: CupService,
    private val userService: UserService
) {
    @Async
    @TransactionalEventListener(NotificationSaveEvent::class)
    fun handleNotificationSaveEvent(event: NotificationSaveEvent) {
        notificationService.save(event.userId, event.type, event.title, event.content, event.targetId)
        notificationService.alert(event.userId)
    }

    @Async
    @TransactionalEventListener(FirstRentalUsedEvent::class)
    fun handleFirstRentalUsedEvent(event: FirstRentalUsedEvent) {
        userService.rewardInviterByInvitee(event.userId)
    }

    // TODO: Remove
    @EventListener(DummyInitEvent::class)
    fun handleContextRefreshedEvent(event: DummyInitEvent) {
        adminAccountService.createDummyAdmin("admin", "qwer1234", "machine", "qwer1234")
        accountService.createDummyUser("010-7777-7777")
        machineService.createDummyMachine("111-1111", "222-2222")
        cupService.createDummyCup(listOf("00 00 00 00","11 11 11 11", "22 22 22 22", "33 33 33 33", "44 44 44 44", "55 55 55 55", "66 66 66 66", "77 77 77 77", "88 88 88 88", "99 99 99 99"))
    }
}