package com.liah.doribottle.repository.user

import com.liah.doribottle.domain.user.BlockedCause
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface BlockedCauseRepository : JpaRepository<BlockedCause, UUID>