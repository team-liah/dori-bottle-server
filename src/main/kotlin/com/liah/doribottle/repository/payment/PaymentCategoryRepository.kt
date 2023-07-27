package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.PaymentCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PaymentCategoryRepository : JpaRepository<PaymentCategory, UUID>