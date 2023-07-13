package com.liah.doribottle.service.rental

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.point.PointQueryRepository
import com.liah.doribottle.repository.point.PointRepository
import com.liah.doribottle.repository.rental.RentalRepository
import com.liah.doribottle.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional
class RentalService(
    private val rentalRepository: RentalRepository,
    private val userRepository: UserRepository,
    private val cupRepository: CupRepository,
    private val machineRepository: MachineRepository,
    private val pointQueryRepository: PointQueryRepository
) {
    fun rental(
        userId: UUID,
        cupRfid: String,
        fromMachineId: UUID,
        withIce: Long
    ) {
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val cup = cupRepository.findByRfid(cupRfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)
        val fromMachine = machineRepository.findByIdOrNull(fromMachineId)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)


    }

    private fun usePoint(userId: UUID, amounts: Long) {
        var remain = amounts
        val points = pointQueryRepository.findAllRemainByUserId(userId)
        points.forEach { point ->
            remain = point.use(amounts)
            if (remain == 0L) return@forEach
        }
    }
}