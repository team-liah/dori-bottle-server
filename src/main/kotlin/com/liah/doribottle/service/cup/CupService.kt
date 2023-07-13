package com.liah.doribottle.service.cup

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupRepository
import com.liah.doribottle.service.cup.dto.CupDto
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class CupService(
    private val cupRepository: CupRepository
) {
    /**
     * Register cup by rfid
     *
     * @param rfid cup's rfid
     * @return result cup's id
     */
    fun register(
        rfid: String
    ): UUID {
        val cup = cupRepository.save(Cup(rfid))

        return cup.id
    }

    /**
     * Get cup by rfid
     *
     * @param rfid cup's rfid
     * @return cup dto
     * @throws NotFoundException if no value is present
     */
    @Transactional(readOnly = true)
    fun getByRfid(
        rfid: String
    ): CupDto {
        val cup = cupRepository.findByRfid(rfid)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)

        return cup.toDto()
    }

    /**
     * Delete cup by id
     *
     * @param id cup's id
     * @throws NotFoundException if no value is present
     * @throws IllegalArgumentException if cup state is on loan
     */
    fun remove(
        id: UUID
    ) {
        val cup = cupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.CUP_NOT_FOUND)
        cup.delete()
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
}