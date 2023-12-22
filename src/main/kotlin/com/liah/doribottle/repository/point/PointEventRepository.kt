package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.PointEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PointEventRepository : JpaRepository<PointEvent, UUID> {
    fun findAllByPointId(pointId: UUID): List<PointEvent>
    fun findAllByTargetId(targetId: UUID): List<PointEvent>
}