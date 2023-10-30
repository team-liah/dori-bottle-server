package com.liah.doribottle.repository.group

import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.group.QGroup.Companion.group
import com.liah.doribottle.domain.user.QUser.Companion.user
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class GroupQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        name: String? = null,
        type: GroupType? = null,
        pageable: Pageable
    ): Page<Group> {
        return queryFactory
            .selectFrom(group)
            .where(
                nameContains(name),
                typeEq(type)
            )
            .toPage(pageable)
    }

    fun findByUserId(
        userId: UUID
    ): Group? {
        return queryFactory
            .selectFrom(group)
            .innerJoin(user).on(group.id.eq(user.group.id))
            .where(user.id.eq(userId))
            .fetchOne()
    }

    private fun nameContains(name: String?) = name?.let { group.name.contains(it) }
    private fun typeEq(type: GroupType?) = type?.let { group.type.eq(it) }
}