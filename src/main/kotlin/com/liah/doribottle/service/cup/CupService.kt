package com.liah.doribottle.service.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.repository.cup.CupQueryRepository
import com.liah.doribottle.repository.cup.CupRepository
import com.liah.doribottle.service.cup.dto.CupDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CupService(
    private val cupRepository: CupRepository,
    private val cupQueryRepository: CupQueryRepository
) {
    fun register(
        rfid: String
    ): UUID {
        verifyDuplicatedRfid(rfid)

        val cup = cupRepository.save(Cup(rfid))

        return cup.id
    }

    private fun verifyDuplicatedRfid(rfid: String) {
        val cup = cupRepository.findByRfid(rfid)
        if (cup != null)
            throw BusinessException(ErrorCode.CUP_ALREADY_REGISTERED)
    }

    @Transactional(readOnly = true)
    fun getByRfid(
        rfid: String
    ): CupDto {
        val cup = cupRepository.findByRfid(rfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)

        return cup.toDto()
    }

    @Transactional(readOnly = true)
    fun getAll(
        status: CupStatus? = null,
        pageable: Pageable
    ): Page<CupDto> {
        return cupQueryRepository.getAll(
            status = status,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun remove(
        id: UUID
    ) {
        val cup = cupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)
        if (cup.verifyOnLoan())
            throw BusinessException(ErrorCode.CUP_DELETE_NOT_ALLOWED)

        cupRepository.delete(cup)
    }

    @Transactional(readOnly = true)
    fun findAllCups(
        pageable: Pageable
    ) {
        // TODO: find all cups
    }

    @Transactional(readOnly = true)
    fun findCupsInMachine(
        machineId: UUID
    ) {
        // TODO: find cups in machine
    }

    @Transactional(readOnly = true)
    fun findCupsUserHas(
        userId: Long
    ) {
        // TODO: find cups user has
    }

    //TODO: Remove
    fun createDummyCup(rfidList: List<String>) {
        rfidList.forEach {
            val cup = cupRepository.findByRfid(it)
            if (cup == null) {
                register(it)
            }
        }
    }
}