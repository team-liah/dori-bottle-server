package com.liah.doribottle.service.rental

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.point.PointEventType.USE_CUP
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.point.PointHistoryRepository
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.rental.RentalQueryRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.rental.dto.RentalDto
import org.springframework.cache.annotation.CacheEvict
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
    private val pointQueryRepository: PointQueryRepository,
    private val pointHistoryRepository: PointHistoryRepository
) {
    @CacheEvict(value = ["pointSum"], key = "#userId")
    fun rent(
        userId: UUID,
        fromMachineNo: String,
        withIce: Boolean
    ): UUID {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val fromMachine = machineRepository.findByNo(fromMachineNo)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        val rental = rentalRepository.save(Rental(user, fromMachine, withIce, 14))
        usePoint(user.id, rental.cost)

        return rental.id
    }

    private fun usePoint(userId: UUID, cost: Long) {
        var remain = cost
        val points = pointQueryRepository.getAllRemainByUserId(userId)
        points.forEach { point ->
            remain = point.use(remain)
            if (remain == 0L) {
                pointHistoryRepository.save(PointHistory(userId, USE_CUP, -cost))
                return
            }
        }
        throw BusinessException(ErrorCode.LACK_OF_POINT)
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
        toMachineId: UUID,
        cupRfid: String
    ) {
        val toMachine = machineRepository.findByIdOrNull(toMachineId)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)
        val cup = cupRepository.findByRfid(cupRfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)
        val rental = rentalRepository.findFirstByCupIdAndStatus(cup.id, PROCEEDING)
            ?: throw NotFoundException(ErrorCode.RENTAL_NOT_FOUND)

        rental.`return`(toMachine)
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