package com.liah.doribottle.domain.cup

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface CupRepository : JpaRepository<Cup, UUID> {
    fun findByRfid(rfid: String): Cup?
}