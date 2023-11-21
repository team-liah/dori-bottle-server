package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.cup.CupStatus.*
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.*
import java.util.*

@Entity
@Table(
    name = "cup",
    indexes = [Index(name = "IDX_CUP_RFID", columnList = "rfid")]
)
class Cup(
    rfid: String
) : SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    var rfid: String = rfid

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CupStatus = AVAILABLE
        protected set

    fun toDto() = CupDto(id, rfid, status, createdDate, lastModifiedDate)

    override fun delete() {
        if (verifyOnLoan())
            throw BusinessException(ErrorCode.CUP_DELETE_NOT_ALLOWED)

        this.rfid = "Deleted ${UUID.randomUUID()}"
        super.delete()
    }

    fun update(
        rfid: String,
        status: CupStatus
    ) {
        this.rfid = rfid
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

    private fun verifyOnLoan() = status == ON_LOAN
    private fun verifyAvailable() = status == AVAILABLE
}