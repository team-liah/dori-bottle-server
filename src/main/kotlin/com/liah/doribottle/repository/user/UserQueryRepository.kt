package com.liah.doribottle.repository.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.QUser.Companion.user
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
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

    fun getAll(
        name: String? = null,
        phoneNumber: String? = null,
        birthDate: String? = null,
        gender: Gender? = null,
        active: Boolean? = null,
        blocked: Boolean? = null,
        groupId: UUID? = null,
        pageable: Pageable
    ): Page<User> {
        return queryFactory
            .selectFrom(user)
            .leftJoin(user.group).fetchJoin()
            .where(
                nameContains(name),
                phoneNumberContains(phoneNumber),
                birthDateContains(birthDate),
                genderEq(gender),
                activeEq(active),
                blockedEq(blocked),
                groupIdEq(groupId),
            )
            .toPage(pageable)
    }

    private fun nameContains(name: String?) = name?.let { user.name.contains(it) }
    private fun phoneNumberContains(phoneNumber: String?) = phoneNumber?.let { user.phoneNumber.contains(it) }
    private fun birthDateContains(birthDate: String?) = birthDate?.let { user.birthDate.contains(it) }
    private fun genderEq(gender: Gender?) = gender?.let { user.gender.eq(it) }
    private fun activeEq(active: Boolean?) = active?.let { user.active.eq(it) }
    private fun blockedEq(blocked: Boolean?) = blocked?.let { user.blocked.eq(it) }
    private fun groupIdEq(groupId: UUID?) = groupId?.let { user.group.id.eq(it) }
}