package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.cup.CupStatus.*
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.*

@Entity
@Table(
    name = "cup",
    indexes = [Index(name = "IDX_CUP_RFID", columnList = "rfid")]
)
class Cup(
    rfid: String
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val rfid: String = rfid

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CupStatus = AVAILABLE
        protected set

    fun toDto() = CupDto(id, rfid, status, createdDate, lastModifiedDate)

    // TODO: 제거
    fun changeState(
        status: CupStatus
    ) {
        this.status = status
    }

    fun loan() {
        if (!verifyAvailable()) throw BusinessException(ErrorCode.CUP_LOAN_NOT_ALLOWED)
        this.status = ON_LOAN
    }

    fun `return`() {
        this.status = RETURNED
    }

    fun lost() {
        this.status = LOST
    }

    fun verifyOnLoan() = status == ON_LOAN
    fun verifyAvailable() = status == AVAILABLE
}