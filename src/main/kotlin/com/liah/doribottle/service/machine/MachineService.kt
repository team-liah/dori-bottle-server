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
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.service.machine.dto.MachineSimpleDto
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
        location: LocationDto,
        capacity: Int,
        imageUrl: String? = null
    ): UUID {
        verifyDuplicatedNo(no)

        val machine = machineRepository.save(
            Machine(
                no = no,
                name = name,
                type = type,
                address = address.toEmbeddable(),
                location = location.toEmbeddable(),
                capacity = capacity,
                imageUrl = imageUrl
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

    @Transactional(readOnly = true)
    fun getAllSimple(
        no: String? = null,
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<MachineSimpleDto> {
        return machineQueryRepository.getAll(
            no = no,
            name = name,
            type = type,
            state = state,
            addressKeyword = addressKeyword,
            deleted = deleted,
            pageable = pageable
        ).map { it.toSimpleDto() }
    }

    fun update(
        id: UUID,
        name: String,
        address: AddressDto,
        location: LocationDto,
        capacity: Int,
        cupAmounts: Int,
        state: MachineState,
        imageUrl: String? = null
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.update(
            name = name,
            address = address.toEmbeddable(),
            location = location.toEmbeddable(),
            capacity = capacity,
            cupAmounts = cupAmounts,
            state = state,
            imageUrl = imageUrl
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
            register(no, "삼성역점", MachineType.VENDING, AddressDto("12345", "서울시", "삼성동"), LocationDto(37.508855, 127.059479), 100, null)
        }

        val collection = machineRepository.findByNo(no2)
        if (collection == null) {
            register(no2, "코엑스점", MachineType.COLLECTION, AddressDto("12345", "서울시", "삼성동"), LocationDto(37.508276, 127.055314), 100, null)
        }
    }
}