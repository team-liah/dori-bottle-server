package com.liah.doribottle.repository.cup

import com.liah.doribottle.domain.cup.Cup
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.history.RevisionRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CupRepository : JpaRepository<Cup, UUID>, RevisionRepository<Cup, UUID, Long> {
    fun findByRfid(rfid: String): Cup?
}
