package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.Point
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.point.PointSaveType.PAY
import com.liah.doribottle.domain.point.PointSaveType.REWARD
import com.liah.doribottle.domain.point.QPoint
import com.liah.doribottle.domain.point.QPoint.Companion.point
import com.liah.doribottle.service.point.dto.PointSumDto
import com.liah.doribottle.service.point.dto.QPointSumDto
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.JPQLQuery
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
     * TODO: If type issue fixed, edit ne to gt
     * https://github.com/querydsl/querydsl/pull/3346
     */
    fun findAllRemainByUserId(userId: UUID): List<Point> {
        return defaultPointQuery(userId)
            .where(point.remainAmounts.ne(0))
            .fetch()
    }

    fun getSumByUserId(userId: UUID): PointSumDto {
        val paySumQuery = generateSumSubQuery(userId, PAY)
        val rewardSumQuery = generateSumSubQuery(userId, REWARD)
        return queryFactory
            .select(
                QPointSumDto(
                    userId = point.userId,
                    totalPayAmounts = paySumQuery,
                    totalRewordAmounts = rewardSumQuery
                )
            )
            .from(point)
            .where(point.userId.eq(userId))
            .fetchFirst()
    }

    private fun generateSumSubQuery(userId: UUID, type: PointSaveType): JPQLQuery<Long> {
        val sub = QPoint(type.name)
        return JPAExpressions
            .select(sub.remainAmounts.sum())
            .from(sub)
            .where(
                sub.userId.eq(userId),
                sub.saveType.eq(type),
                sub.remainAmounts.ne(0)
            )
    }
}