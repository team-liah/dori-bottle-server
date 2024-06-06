package com.liah.doribottle.domain.cup

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.service.cup.dto.CupDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.hibernate.envers.AuditOverride
import org.hibernate.envers.Audited

@Audited
@AuditOverride(forClass = PrimaryKeyEntity::class)
@AuditOverride(forClass = SoftDeleteEntity::class)
@Entity
@Table(
    name = "cup",
    indexes = [Index(name = "IDX_CUP_RFID", columnList = "rfid")],
)
class Cup(
    rfid: String,
) : SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    var rfid: String = rfid
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CupStatus = CupStatus.AVAILABLE
        protected set

    override fun delete() {
        if (verifyOnLoan()) {
            throw BusinessException(ErrorCode.CUP_DELETE_NOT_ALLOWED)
        }

        this.rfid = "Deleted $id"
        super.delete()
    }

    fun update(
        rfid: String,
        status: CupStatus,
    ) {
        this.rfid = rfid
        this.status = status
    }

    fun loan() {
        if (!verifyAvailable()) throw BusinessException(ErrorCode.CUP_LOAN_NOT_ALLOWED)
        this.status = CupStatus.ON_LOAN
    }

    fun `return`() {
        this.status = CupStatus.RETURNED
    }

    fun lost() {
        this.status = CupStatus.LOST
    }

    private fun verifyOnLoan() = status == CupStatus.ON_LOAN

    private fun verifyAvailable() = status == CupStatus.AVAILABLE

    fun toDto() =
        CupDto(
            id = id,
            rfid = rfid,
            status = status,
            createdDate = createdDate,
            lastModifiedDate = lastModifiedDate,
        )
}
