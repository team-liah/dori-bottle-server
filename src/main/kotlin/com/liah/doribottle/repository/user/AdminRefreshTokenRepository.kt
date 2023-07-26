package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.AdminRefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface AdminRefreshTokenRepository : JpaRepository<AdminRefreshToken, UUID> {
    fun findByTokenAndExpiredDateIsAfter(token: String?, expiredDate: Instant): AdminRefreshToken?
}