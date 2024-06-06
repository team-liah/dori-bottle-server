package com.liah.doribottle.service.cup.dto

import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import org.springframework.data.history.Revision
import java.time.Instant
import java.util.UUID

data class CupRevisionDto(
    val revisionNumber: Long?,
    val id: UUID?,
    val rfid: String?,
    val status: CupStatus?,
    val deleted: Boolean?,
    val createdDate: Instant?,
    val lastModifiedDate: Instant?,
    val createdBy: UUID?,
    val lastModifiedBy: UUID?,
) {
    companion object {
        fun fromRevision(revision: Revision<Long, Cup>): CupRevisionDto {
            return CupRevisionDto(
                revisionNumber = revision.metadata.revisionNumber.orElse(null),
                id = revision.entity.id,
                rfid = revision.entity.rfid,
                status = revision.entity.status,
                deleted = revision.entity.deleted,
                createdDate = revision.entity.createdDate,
                lastModifiedDate = revision.entity.lastModifiedDate,
                createdBy = revision.entity.createdBy,
                lastModifiedBy = revision.entity.lastModifiedBy,
            )
        }
    }
}
