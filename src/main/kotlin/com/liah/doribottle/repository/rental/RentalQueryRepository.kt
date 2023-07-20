package com.liah.doribottle.repository.rental

import com.liah.doribottle.domain.rental.QRental.Companion.rental
import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
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
            .where(
                userEq(userId),
                cupEq(cupId),
                fromMachineEq(fromMachineId),
                toMachineEq(toMachineId),
                statusEq(status),
                expired(expired)
            )
            .toPage(pageable)
    }

    private fun userEq(userId: UUID?) = userId?.let { rental.user.id.eq(it) }
    private fun cupEq(cupId: UUID?) = cupId?.let { rental.cup.id.eq(it) }
    private fun fromMachineEq(fromMachineId: UUID?) = fromMachineId?.let { rental.fromMachine.id.eq(it) }
    private fun toMachineEq(toMachineId: UUID?) = toMachineId?.let { rental.toMachine.id.eq(it) }
    private fun statusEq(status: RentalStatus?) = status?.let { rental.status.eq(it) }
    private fun expired(expired: Boolean?) = expired?.let {
        val now = Instant.now()
        if (expired) rental.expiredDate.before(now)
        else rental.expiredDate.after(now)
    }
}