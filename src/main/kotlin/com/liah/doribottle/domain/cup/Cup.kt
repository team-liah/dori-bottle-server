package com.liah.doribottle.domain.cup

import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.cup.CupState.ON_LOAN
import com.liah.doribottle.domain.cup.CupState.PENDING
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

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
        private set

    fun toDto() = CupDto(id, rfid, state, deletedReason, deletedDate, deletedBy)

    fun delete(
        reason: String?
    ) {
        if (verifyOnLoan()) throw IllegalArgumentException("대여 중인 컵은 삭제할 수 없습니다.")
        deletedReason = reason
        deletedBy = "User principal" // TODO: get user principal from security context
        deletedDate = Instant.now()
    }

    fun changeState(
        state: CupState
    ) {
        this.state = state
    }

    private fun verifyOnLoan() = state == ON_LOAN
}