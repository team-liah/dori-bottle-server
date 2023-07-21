package com.liah.doribottle.repository.cup

import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.cup.CupStatus
import com.liah.doribottle.domain.cup.QCup.Companion.cup
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class CupQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        status: CupStatus? = null,
        pageable: Pageable
    ): Page<Cup> {
        return queryFactory
            .selectFrom(cup)
            .where(
                statusEq(status)
            )
            .toPage(pageable)
    }

    private fun statusEq(status: CupStatus?) = status?.let { cup.status.eq(status) }
}