package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.Point
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PointRepository : JpaRepository<Point, UUID> {
    fun findByUserId(userId: UUID): Point?
}