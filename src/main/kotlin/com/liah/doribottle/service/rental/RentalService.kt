package com.liah.doribottle.service.rental

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.ForbiddenException
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.notification.NotificationIndividual
import com.liah.doribottle.domain.notification.NotificationType
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.event.Events
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.rental.RentalQueryRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.service.rental.dto.RentalDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class RentalService(
    private val rentalRepository: RentalRepository,
    private val rentalQueryRepository: RentalQueryRepository,
    private val userRepository: UserRepository,
    private val cupRepository: CupRepository,
    private val machineRepository: MachineRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val pointService: PointService
) {
    fun rent(
        userId: UUID,
        fromMachineNo: String,
        withIce: Boolean
    ): UUID {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val fromMachine = machineRepository.findByNo(fromMachineNo)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        verifyCanRent(user)

        val rental = rentalRepository.save(Rental(user, fromMachine, withIce, 14))
        pointService.use(user.id, rental.cost)

        if (!user.use) {
            user.use()
            Events.useFirstRental(user.id)
        }

        return rental.id
    }

    private fun verifyCanRent(user: User) {
        if (user.blocked)
            throw ForbiddenException(ErrorCode.BLOCKED_USER_ACCESS_DENIED)
        paymentMethodRepository.findFirstByUserIdAndDefault(user.id, true)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)
    }

    fun updateRentalCup(
        id: UUID,
        cupRfid: String
    ) {
        val rental = rentalRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.RENTAL_NOT_FOUND)
        val cup = cupRepository.findByRfid(cupRfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)

        rental.setRentalCup(cup)
    }

    fun `return`(
        toMachineNo: String,
        cupRfid: String
    ) {
        val toMachine = machineRepository.findByNo(toMachineNo)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)
        val cup = cupRepository.findByRfid(cupRfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)
        val rental = rentalQueryRepository.findLastByCupId(cup.id)
            ?: throw NotFoundException(ErrorCode.RENTAL_NOT_FOUND)

        rental.`return`(toMachine)
    }

    fun fail(
        id: UUID
    ) {
        val rental = rentalRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.RENTAL_NOT_FOUND)

        rental.fail()

        Events.notify(
            NotificationIndividual(
                userId = rental.user.id,
                type = NotificationType.LOST_CUP,
                targetId = rental.id
            )
        )
    }

    @Transactional(readOnly = true)
    fun getAll(
        userId: UUID? = null,
        cupId: UUID? = null,
        fromMachineId: UUID? = null,
        toMachineId: UUID? = null,
        status: RentalStatus? = null,
        expired: Boolean? = null,
        pageable: Pageable
    ): Page<RentalDto> {
        return rentalQueryRepository.getAll(
            userId = userId,
            cupId = cupId,
            fromMachineId = fromMachineId,
            toMachineId = toMachineId,
            status = status,
            expired = expired,
            pageable = pageable
        ).map { it.toDto() }
    }
}