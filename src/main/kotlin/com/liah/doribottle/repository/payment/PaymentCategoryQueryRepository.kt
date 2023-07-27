package com.liah.doribottle.repository.payment

import com.liah.doribottle.domain.payment.PaymentCategory
import com.liah.doribottle.domain.payment.QPaymentCategory.Companion.paymentCategory
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class PaymentCategoryQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        expired: Boolean? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<PaymentCategory> {
        return queryFactory
            .selectFrom(paymentCategory)
            .where(
                expired(expired),
                deleted(deleted)
            )
            .toPage(pageable)
    }

    private fun expired(expired: Boolean?) = expired?.let {
        if (it) {
            paymentCategory.expiredDate.before(Instant.now())
        } else {
            paymentCategory.expiredDate.isNull.or(paymentCategory.expiredDate.after(Instant.now()))
        }
    }

    private fun deleted(deleted: Boolean?) = deleted?.let { paymentCategory.deleted.eq(deleted) }
}