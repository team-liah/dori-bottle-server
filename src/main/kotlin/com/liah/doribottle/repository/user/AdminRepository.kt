package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.Admin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdminRepository : JpaRepository<Admin, UUID> {
    fun findByLoginId(loginId: String): Admin?
    fun findAllByGroupId(groupId: UUID): List<Admin>
}