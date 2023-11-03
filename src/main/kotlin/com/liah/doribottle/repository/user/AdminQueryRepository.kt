package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.QAdmin.Companion.admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class AdminQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        loginId: String? = null,
        name: String? = null,
        role: Role? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<Admin> {
        return queryFactory
            .selectFrom(admin)
            .where(
                loginIdContains(loginId),
                nameContains(name),
                roleEq(role),
                deletedEq(deleted)
            )
            .toPage(pageable)
    }

    private fun loginIdContains(loginId: String?) = loginId?.let { admin.loginId.contains(it) }
    private fun nameContains(name: String?) = name?.let { admin.name.contains(it) }
    private fun roleEq(role: Role?) = role?.let { admin.role.eq(it) }
    private fun deletedEq(deleted: Boolean?) = deleted?.let { admin.deleted.eq(it) }
}