package com.liah.doribottle.config.security

import org.springframework.data.repository.CrudRepository
import java.util.*

interface RefreshTokenRepository : CrudRepository<RefreshToken, UUID>