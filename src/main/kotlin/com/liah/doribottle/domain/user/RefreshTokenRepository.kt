package com.liah.doribottle.domain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByUserIdAndTokenAndExpiredDateIsAfter(userId: UUID, token: String?, expiredDate: Instant): RefreshToken?
}