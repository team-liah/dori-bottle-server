package com.liah.doribottle.repository.group

import com.liah.doribottle.domain.group.Group
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GroupRepository : JpaRepository<Group, UUID> {
    fun findByCode(code: String): Group?
}