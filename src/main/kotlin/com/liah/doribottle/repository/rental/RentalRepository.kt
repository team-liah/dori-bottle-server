package com.liah.doribottle.repository.rental

import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface RentalRepository : JpaRepository<Rental, UUID> {
    fun findAllByExpiredDateBetweenAndStatusAndCupIsNotNull(start: Instant, end: Instant, status: RentalStatus): List<Rental>
}