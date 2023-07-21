package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.repository.machine.MachineQueryRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.machine.dto.MachineDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class MachineService(
    private val machineRepository: MachineRepository,
    private val machineQueryRepository: MachineQueryRepository
) {
    fun register(
        no: String,
        name: String,
        type: MachineType,
        address: Address,
        capacity: Int
    ): UUID {
        val machine = machineRepository.save(Machine(no, name, type, address, capacity))

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

    @Transactional(readOnly = true)
    fun getAll(
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        pageable: Pageable
    ): Page<MachineDto> {
        return machineQueryRepository.getAll(
            name = name,
            type = type,
            state = state,
            addressKeyword = addressKeyword,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun update(
        id: UUID,
        name: String,
        address: Address,
        capacity: Int,
        cupAmounts: Int
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.update(name, address, capacity)
        machine.updateCupAmounts(cupAmounts)
    }
}