package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.QPoint.Companion.point
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PointQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private fun defaultPointQuery(userId: UUID) = queryFactory
        .selectFrom(point)
        .where(point.userId.eq(userId))
        .orderBy(point.saveType.desc())

    /**
     * Find remain points by user id
     */
    fun findAllRemainByUserId(userId: UUID): List<Point> {
        return defaultPointQuery(userId)
            .where(point.remainAmounts.gt(0))
            .fetch()
    }
}