package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.PaymentMethod
import com.liah.doribottle.domain.payment.QPaymentMethod.Companion.paymentMethod
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PaymentMethodQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        userId: UUID,
        pageable: Pageable
    ): Page<PaymentMethod> {
        return queryFactory
            .selectFrom(paymentMethod)
            .where(
                paymentMethod.user.id.eq(userId)
            )
            .toPage(pageable)
    }
}