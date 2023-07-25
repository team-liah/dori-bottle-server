package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.PointHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PointHistoryRepository : JpaRepository<PointHistory, UUID> {
    fun findAllByUserId(userId: UUID): List<PointHistory>
}