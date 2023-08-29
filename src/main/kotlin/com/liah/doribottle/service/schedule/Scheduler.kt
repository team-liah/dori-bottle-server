package com.liah.doribottle.service.schedule

import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.service.payment.PaymentService
import com.liah.doribottle.service.rental.RentalService
import org.springframework.data.domain.Pageable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class Scheduler(
    private val rentalService: RentalService,
    private val paymentService: PaymentService
) {
    // TODO: Test
    @Scheduled(fixedDelay = 60000)
    fun payToLostCupTask() {
        val expiredRentals = rentalService.getAll(
            status = RentalStatus.PROCEEDING,
            expired = true,
            pageable = Pageable.unpaged()
        ).content

        expiredRentals.forEach { rental ->
            runCatching {
                rentalService.fail(
                    id = rental.id
                )
            }.onSuccess {
                runCatching {
                    val method = paymentService.getDefaultMethod(rental.userId)
                    paymentService.create(
                        userId = rental.userId,
                        price = 5000,
                        type = PaymentType.LOST_CUP,
                        card = method.card
                    )
                }
            }

        }
    }
}