package com.liah.doribottle.repository.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.QUser.Companion.user
import com.liah.doribottle.domain.user.User
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun get(id: UUID): User {
        return queryFactory
            .selectFrom(user)
            .leftJoin(user.mutablePenalties).fetchJoin()
            .leftJoin(user.group).fetchJoin()
            .where(user.id.eq(id))
            .fetchOne() ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
    }
}