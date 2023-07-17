package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.machine.dto.MachineDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class MachineService(
    private val machineRepository: MachineRepository
) {
    fun register(
        no: String,
        type: MachineType,
        address: Address,
        capacity: Int
    ): UUID {
        val machine = machineRepository.save(Machine(no, type, address, capacity))

        return machine.id
    }

    @Transactional(readOnly = true)
    fun get(
        id: UUID
    ): MachineDto {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        return machine.toDto()
    }

    fun update(
        id: UUID,
        address: Address,
        capacity: Int,
        cupAmounts: Int
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.update(address, capacity)
        machine.updateCupAmounts(cupAmounts)
    }
}