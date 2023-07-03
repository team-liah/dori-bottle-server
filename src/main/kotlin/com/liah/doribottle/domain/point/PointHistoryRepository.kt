package com.liah.doribottle.domain.point

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PointHistoryRepository : JpaRepository<PointHistory, UUID> {
    fun findAllByPointId(pointId: UUID): List<PointHistory>
}