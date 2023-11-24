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
        rfid: String? = null,
        status: CupStatus? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<Cup> {
        return queryFactory
            .selectFrom(cup)
            .where(
                rfidContains(rfid),
                statusEq(status),
                deletedEq(deleted)
            )
            .toPage(pageable)
    }

    private fun rfidContains(rfid: String?) = rfid?.let { cup.rfid.contains(it) }
    private fun statusEq(status: CupStatus?) = status?.let { cup.status.eq(it) }
    private fun deletedEq(deleted: Boolean?) = deleted?.let { cup.deleted.eq(it) }
}