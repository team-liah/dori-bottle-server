package com.liah.doribottle.config.security

import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.extension.systemId
import org.springframework.data.domain.AuditorAware
import java.util.*

class AuditorAwareImpl : AuditorAware<UUID> {
    override fun getCurrentAuditor(): Optional<UUID> {
        return Optional.of(currentUserId() ?: systemId())
    }
}