package com.liah.doribottle.repository.group

import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.group.QGroup.Companion.group
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

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

    private fun nameContains(name: String?) = name?.let { group.name.contains(it) }
    private fun typeEq(type: GroupType?) = type?.let { group.type.eq(it) }
}