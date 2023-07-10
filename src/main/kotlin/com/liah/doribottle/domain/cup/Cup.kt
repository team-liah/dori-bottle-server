package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.cup.CupState.ON_LOAN
import com.liah.doribottle.domain.cup.CupState.PENDING
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

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
    var state: CupState = PENDING
        protected set

    fun toDto() = CupDto(id, rfid, state)

    fun delete() {
        if (verifyOnLoan()) throw BusinessException(ErrorCode.CUP_DELETE_NOT_ALLOWED)
        deleted = true
    }

    fun changeState(
        state: CupState
    ) {
        this.state = state
    }

    private fun verifyOnLoan() = state == ON_LOAN
}