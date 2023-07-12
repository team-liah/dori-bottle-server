package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.cup.CupStatus.ON_LOAN
import com.liah.doribottle.domain.cup.CupStatus.PENDING
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.*

@Entity
@Table(
    name = "cup",
    indexes = [Index(name = "IDX_CUP_RFID", columnList = "rfid")]
)
class Cup(
    rfid: String
) : SoftDeleteEntity() {
    // TODO: Add current Machine FK Column

    @Column(nullable = false, unique = true)
    val rfid: String = rfid

    @Column(nullable = false)
    var status: CupStatus = PENDING
        protected set

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn
    var machine: Machine? = null

    fun toDto() = CupDto(id, rfid, status)

    fun delete() {
        if (verifyOnLoan()) throw BusinessException(ErrorCode.CUP_DELETE_NOT_ALLOWED)
        deleted = true
    }

    fun changeState(
        status: CupStatus
    ) {
        this.status = status
    }

    private fun verifyOnLoan() = status == ON_LOAN
}