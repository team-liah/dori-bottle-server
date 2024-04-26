package com.liah.doribottle.schedule

import com.liah.doribottle.constant.DoriConstant
import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.task.TaskType
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TosspaymentsService
import com.liah.doribottle.service.payment.dto.PaymentMethodDto
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.service.task.TaskService
import com.liah.doribottle.service.task.dto.TaskDto
import com.liah.doribottle.service.user.UserService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Scheduler(
    private val taskService: TaskService,
    private val rentalService: RentalService,
    private val paymentService: PaymentService,
    private val userService: UserService,
    private val tosspaymentsService: TosspaymentsService,
    private val notificationService: NotificationService
) {
    @SchedulerLock(
        name = "scheduledTask",
        lockAtLeastFor = "PT60S",
        lockAtMostFor = "PT60S"
    )
    @Scheduled(fixedDelay = 60000)
    fun scheduledTask() {
        val tasks = taskService.getAllForExecute()
        tasks.forEach { task ->
            taskService.delete(task.id)
            runCatching {
                when (task.type) {
                    TaskType.RENTAL_OVERDUE -> { overdueRental(task) }
                    TaskType.RENTAL_REMIND -> { remindRental(task) }
                }
            }
        }
    }

    /**
     * TASK START
     */

    private fun overdueRental(task: TaskDto) {
        val rental = rentalService.get(task.targetId)
        rentalService.fail(rental.id)
        payToLostCup(rental.user.id)
    }

    private fun remindRental(task: TaskDto) {
        val rental = rentalService.get(task.targetId)
        notificationService.saveAll(listOf(
            NotificationIndividual(
                userId = rental.user.id,
                type = NotificationType.NEAR_EXPIRATION,
                targetId = rental.id,
                rental.no
            )
        ))
        notificationService.alert(rental.user.id)
    }

    /**
     * TASK END
     */

    private fun payToLostCup(userId: UUID) {
        val paymentMethod = runCatching {
            paymentService.getDefaultMethod(userId)
        }.getOrNull()

        if (paymentMethod == null) {
            userService.block(
                id = userId,
                blockedCauseType = BlockedCauseType.LOST_CUP_PENALTY,
                blockedCauseDescription = null
            )
            return
        }

        billing(userId, paymentMethod)
    }

    private fun billing(
        userId: UUID,
        paymentMethod: PaymentMethodDto
    ) {
        val paymentId = paymentService.create(
            userId = userId,
            price = DoriConstant.LOST_CUP_PRICE,
            type = PaymentType.LOST_CUP,
            card = paymentMethod.card
        )

        runCatching {
            tosspaymentsService.executeBilling(
                billingKey = paymentMethod.billingKey,
                userId = userId,
                price = DoriConstant.LOST_CUP_PRICE,
                paymentId = paymentId,
                paymentType = PaymentType.LOST_CUP
            )
        }.onSuccess { result ->
            paymentService.updateResult(
                id = paymentId,
                result = result
            )
        }.onFailure {
            paymentService.updateResult(
                id = paymentId,
                result = null
            )
            userService.block(
                id = userId,
                blockedCauseType = BlockedCauseType.LOST_CUP_PENALTY,
                blockedCauseDescription = null
            )
        }
    }
}