package com.liah.doribottle.repository.point

import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointHistory
import com.liah.doribottle.domain.point.QPointHistory.Companion.pointHistory
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PointHistoryQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        userId: UUID? = null,
        type: PointEventType? = null,
        pageable: Pageable
    ): Page<PointHistory> {
        return queryFactory
            .selectFrom(pointHistory)
            .where(
                userIdEq(userId),
                typeEq(type)
            )
            .toPage(pageable)
    }

    private fun userIdEq(userId: UUID?) = userId?.let { pointHistory.userId.eq(it) }
    private fun typeEq(type: PointEventType?) = type?.let { pointHistory.type.eq(it) }
}