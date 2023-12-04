package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentRepository : JpaRepository<Payment, UUID> {
    @Query(value =
        "select date(convert_tz(p.approved_date, '+00:00', '+09:00')) date, " +
                "sum(if(p.status = 'SUCCEEDED', p.price, 0)) totalAmount, " +
                "sum(if(p.status = 'SUCCEEDED' and p.type = 'SAVE_POINT', p.price, 0)) savePointAmount, " +
                "sum(if(p.status = 'SUCCEEDED' and p.type = 'LOST_CUP', p.price, 0)) lostCupAmount, " +
                "sum(if(p.status = 'SUCCEEDED' and p.type = 'UNBLOCK_ACCOUNT', p.price, 0)) unblockAccountAmount, " +
                "sum(if(p.status = 'CANCELED', p.price, 0)) cancelAmount " +
        "from payment p " +
        "where convert_tz(p.approved_date, '+00:00', '+09:00') >= str_to_date(:startDate, '%Y%m%d') " +
        "  and convert_tz(p.approved_date, '+00:00', '+09:00') <= str_to_date(:endDate, '%Y%m%d') " +
        "group by date",
        nativeQuery = true
    )
    fun findStatisticByApprovedDate(
        @Param("startDate") startDate: String,
        @Param("endDate") endDate: String
    ): List<PaymentStatisticDao>
}