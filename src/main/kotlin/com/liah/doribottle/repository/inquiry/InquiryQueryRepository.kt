package com.liah.doribottle.repository.inquiry

import com.liah.doribottle.domain.inquiry.Inquiry
import com.liah.doribottle.domain.inquiry.InquiryStatus
import com.liah.doribottle.domain.inquiry.InquiryType
import com.liah.doribottle.domain.inquiry.QInquiry.Companion.inquiry
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class InquiryQueryRepository(
    private val queryFactory: JPAQueryFactory,
) {
    fun getAll(
        userId: UUID? = null,
        type: InquiryType? = null,
        status: InquiryStatus? = null,
        keyword: String? = null,
        pageable: Pageable,
    ): Page<Inquiry> {
        return queryFactory
            .selectFrom(inquiry)
            .leftJoin(inquiry.user).fetchJoin()
            .where(
                userEq(userId),
                typeEq(type),
                statusEq(status),
                keywordContains(keyword),
            )
            .toPage(pageable)
    }

    private fun userEq(userId: UUID?) = userId?.let { inquiry.user.id.eq(it) }

    private fun typeEq(type: InquiryType?) = type?.let { inquiry.type.eq(it) }

    private fun statusEq(status: InquiryStatus?) = status?.let { inquiry.status.eq(it) }

    private fun keywordContains(keyword: String?) = keyword?.let { inquiry.content.contains(it).or(inquiry.answer.contains(it)) }
}
