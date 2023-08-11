package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, UUID> {
    fun findByLoginId(loginId: String): User?
    fun findByInvitationCode(invitationCode: String): User?
    fun findAllByGroupId(groupId: UUID): List<User>
}