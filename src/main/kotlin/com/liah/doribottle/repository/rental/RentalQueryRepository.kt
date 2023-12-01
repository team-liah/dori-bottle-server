package com.liah.doribottle.repository.rental

import com.liah.doribottle.domain.rental.QRental.Companion.rental
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
class RentalQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        no: String? = null,
        userId: UUID? = null,
        cupId: UUID? = null,
        fromMachineId: UUID? = null,
        toMachineId: UUID? = null,
        status: RentalStatus? = null,
        expired: Boolean? = null,
        pageable: Pageable
    ): Page<Rental> {
        return queryFactory
            .selectFrom(rental)
            .innerJoin(rental.user).fetchJoin()
            .innerJoin(rental.fromMachine).fetchJoin()
            .leftJoin(rental.toMachine).fetchJoin()
            .where(
                noContains(no),
                userIdEq(userId),
                cupIdEq(cupId),
                fromMachineIdEq(fromMachineId),
                toMachineIdEq(toMachineId),
                statusEq(status),
                expired(expired)
            )
            .where(rental.cup.isNotNull)
            .toPage(pageable)
    }

    fun findLastByCupId(
        cupId: UUID
    ): Rental? {
        return queryFactory
            .selectFrom(rental)
            .where(
                cupIdEq(cupId)
            )
            .where(rental.cup.isNotNull)
            .orderBy(rental.createdDate.desc())
            .fetchFirst()
    }

    fun existsProceedingByUserId(
        userId: UUID
    ): Boolean {
        return queryFactory
            .selectFrom(rental)
            .where(
                userIdEq(userId),
                statusEq(PROCEEDING)
            )
            .where(rental.cup.isNotNull)
            .fetchFirst() != null
    }

    fun existsByNo(
        no: String
    ): Boolean {
        return queryFactory
            .selectFrom(rental)
            .where(
                noEq(no)
            )
            .fetchFirst() != null
    }

    private fun noEq(no: String?) = no?.let { rental.no.eq(it) }
    private fun noContains(no: String?) = no?.let { rental.no.contains(it) }
    private fun userIdEq(userId: UUID?) = userId?.let { rental.user.id.eq(it) }
    private fun cupIdEq(cupId: UUID?) = cupId?.let { rental.cup.id.eq(it) }
    private fun fromMachineIdEq(fromMachineId: UUID?) = fromMachineId?.let { rental.fromMachine.id.eq(it) }
    private fun toMachineIdEq(toMachineId: UUID?) = toMachineId?.let { rental.toMachine.id.eq(it) }
    private fun statusEq(status: RentalStatus?) = status?.let { rental.status.eq(it) }
    private fun expired(expired: Boolean?) = expired?.let {
        val now = Instant.now()
        if (expired) rental.expiredDate.before(now)
        else rental.expiredDate.after(now)
    }
}