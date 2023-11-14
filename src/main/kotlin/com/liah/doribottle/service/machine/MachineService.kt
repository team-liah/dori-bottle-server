package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.repository.machine.MachineQueryRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.common.AddressDto
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
        address: AddressDto,
        capacity: Int
    ): UUID {
        verifyDuplicatedNo(no)

        val machine = machineRepository.save(
            Machine(
                no = no,
                name = name,
                type = type,
                address = address.toEmbeddable(),
                capacity = capacity
            )
        )

        return machine.id
    }

    private fun verifyDuplicatedNo(no: String) {
        val machine = machineRepository.findByNo(no)
        if (machine != null)
            throw BusinessException(ErrorCode.MACHINE_ALREADY_REGISTERED)
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
        no: String? = null,
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<MachineDto> {
        return machineQueryRepository.getAll(
            no = no,
            name = name,
            type = type,
            state = state,
            addressKeyword = addressKeyword,
            deleted = deleted,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun update(
        id: UUID,
        name: String,
        address: AddressDto,
        capacity: Int,
        cupAmounts: Int,
        state: MachineState
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.update(
            name = name,
            address = address.toEmbeddable(),
            capacity = capacity,
            cupAmounts = cupAmounts,
            state = state
        )
    }

    fun delete(
        id: UUID
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.delete()
    }

    // TODO: Remove
    fun createDummyMachine(
        no: String,
        no2: String
    ) {
        val vending = machineRepository.findByNo(no)
        if (vending == null) {
            register(no, "삼성역점", MachineType.VENDING, AddressDto("12345", "서울시", "삼성동"), 100)
        }

        val collection = machineRepository.findByNo(no2)
        if (collection == null) {
            register(no2, "코엑스점", MachineType.COLLECTION, AddressDto("12345", "서울시", "삼성동"), 100)
        }
    }
}