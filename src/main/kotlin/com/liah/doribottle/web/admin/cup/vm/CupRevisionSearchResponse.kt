package com.liah.doribottle.web.admin.cup.vm

import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.service.cup.dto.CupRevisionDto
import java.time.Instant
import java.util.UUID

data class CupRevisionSearchResponse(
    val revisionNumber: Long?,
    val id: UUID?,
    val rfid: String?,
    val status: CupStatus?,
    val deleted: Boolean?,
    val createdDate: Instant?,
    val lastModifiedDate: Instant?,
    val createdBy: DoriUser?,
    val lastModifiedBy: DoriUser?,
) {
    companion object {
        fun fromDto(
            dto: CupRevisionDto,
            extractDoriUser: (UUID?) -> DoriUser?,
        ): CupRevisionSearchResponse {
            return CupRevisionSearchResponse(
                revisionNumber = dto.revisionNumber,
                id = dto.id,
                rfid = dto.rfid,
                status = dto.status,
                deleted = dto.deleted,
                createdDate = dto.createdDate,
                lastModifiedDate = dto.lastModifiedDate,
                createdBy = extractDoriUser(dto.createdBy),
                lastModifiedBy = extractDoriUser(dto.lastModifiedBy),
            )
        }
    }
}
