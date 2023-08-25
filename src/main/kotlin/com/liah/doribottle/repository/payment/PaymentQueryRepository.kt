package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.Payment
import com.liah.doribottle.domain.payment.PaymentStatus
import com.liah.doribottle.domain.payment.PaymentType
import com.liah.doribottle.domain.payment.QPayment.Companion.payment
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class PaymentQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        userId: UUID? = null,
        type: PaymentType? = null,
        statuses: Set<PaymentStatus>? = null,
        pageable: Pageable
    ): Page<Payment> {
        return queryFactory
            .selectFrom(payment)
            .where(
                userIdEq(userId),
                typeEq(type),
                statusIn(statuses),
            )
            .toPage(pageable)
    }

    private fun userIdEq(userId: UUID?) = userId?.let { payment.user.id.eq(it) }
    private fun typeEq(type: PaymentType?) = type?.let { payment.type.eq(it) }
    private fun statusIn(statuses: Set<PaymentStatus>?) = statuses?.let { payment.status.`in`(it) }
}