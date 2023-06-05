package com.liah.doribottle.domain.admin

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface AdminRepository : JpaRepository<Admin, UUID> {
    fun findByLoginId(loginId: String): Optional<Admin>
}