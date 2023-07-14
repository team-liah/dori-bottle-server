package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.PointSum
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PointSumRepository : JpaRepository<PointSum, UUID> {
    fun findByUserId(userId: UUID): PointSum?
}