package com.liah.doribottle.repository.rental

import com.liah.doribottle.domain.rental.Rental
import com.liah.doribottle.domain.rental.RentalStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface RentalRepository : JpaRepository<Rental, UUID> {
    fun findAllByExpiredDateBetweenAndStatusAndCupIsNotNull(
        start: Instant,
        end: Instant,
        status: RentalStatus,
    ): List<Rental>

    @Query(
        value =
            "select date_format(convert_tz(r.created_date, '+00:00', '+09:00'), :groupFormat) date, " +
                "sum(if(r.status != 'CANCELED', r.cost, 0)) totalPointAmount, " +
                "sum(if(r.status = 'CONFIRMED', 1, 0)) confirmedCount, " +
                "sum(if(r.status = 'SUCCEEDED', 1, 0)) succeededCount, " +
                "sum(if(r.status = 'FAILED', 1, 0)) failedCount, " +
                "sum(if(r.status = 'CANCELED', 1, 0)) canceledCount " +
                "from rental r " +
                "where convert_tz(r.created_date, '+00:00', '+09:00') >= str_to_date(:startDate, '%Y%m%d') " +
                "  and convert_tz(r.created_date, '+00:00', '+09:00') < str_to_date(:endDate, '%Y%m%d') " +
                "group by date " +
                "order by date",
        nativeQuery = true,
    )
    fun findStatisticByCreatedDate(
        @Param("groupFormat") groupFormat: String,
        @Param("startDate") startDate: String,
        @Param("endDate") endDate: String,
    ): List<RentalStatisticDao>
}
