package com.liah.doribottle.schedule

import com.liah.doribottle.constant.LOST_CUP_PRICE
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.payment.TossPaymentsService
import com.liah.doribottle.service.payment.dto.PaymentMethodDto
import com.liah.doribottle.service.rental.RentalService
import com.liah.doribottle.service.user.UserService
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.*

@Component
class Scheduler(
    private val rentalService: RentalService,
    private val paymentService: PaymentService,
    private val userService: UserService,
    private val tossPaymentsService: TossPaymentsService
) {
    @Scheduled(fixedDelay = 60000)
    fun payToLostCupTask() {
        val expiredRentals = rentalService.getAll(
            status = RentalStatus.PROCEEDING,
            expired = true,
            pageable = Pageable.unpaged()
        ).content

        expiredRentals.forEach { rental ->
            runCatching {
                rentalService.fail(rental.id)
            }.onSuccess {
                payToLostCup(rental.userId)
            }
        }
    }

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
            price = LOST_CUP_PRICE,
            type = PaymentType.LOST_CUP,
            card = paymentMethod.card
        )

        runCatching {
            tossPaymentsService.executeBilling(
                billingKey = paymentMethod.billingKey,
                userId = userId,
                price = LOST_CUP_PRICE,
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