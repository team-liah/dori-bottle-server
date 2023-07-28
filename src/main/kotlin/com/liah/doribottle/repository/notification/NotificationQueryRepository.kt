package com.liah.doribottle.repository.notification

import com.liah.doribottle.domain.notification.Notification
import com.liah.doribottle.domain.notification.QNotification.Companion.notification
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class NotificationQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        userId: UUID,
        pageable: Pageable
    ): Page<Notification> {
        return queryFactory
            .selectFrom(notification)
            .where(notification.userId.eq(userId))
            .toPage(pageable)
    }
}