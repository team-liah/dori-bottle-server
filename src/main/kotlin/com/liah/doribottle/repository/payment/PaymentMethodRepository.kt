package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.PaymentMethod
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentMethodRepository : JpaRepository<PaymentMethod, UUID> {
    fun findFirstByUserIdAndDefaultIsTrue(userId: UUID): PaymentMethod?
}