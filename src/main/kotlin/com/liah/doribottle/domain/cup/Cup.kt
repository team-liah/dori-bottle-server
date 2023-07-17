package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.cup.CupStatus.*
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(
    name = "cup",
    indexes = [Index(name = "IDX_CUP_RFID", columnList = "rfid")]
)
@SQLDelete(sql = "UPDATE cup SET deleted = true where id = ?")
@Where(clause = "deleted = false")
class Cup(
    rfid: String
) : SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    val rfid: String = rfid

    @Column(nullable = false)
    var status: CupStatus = INITIAL
        protected set

    fun toDto() = CupDto(id, rfid, status)

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
        if (!verifyOnLoan()) throw BusinessException(ErrorCode.CUP_RETURN_NOT_ALLOWED)
        this.status = RETURNED
    }

    fun verifyOnLoan() = status == ON_LOAN
    fun verifyAvailable() = status == AVAILABLE
}