package com.liah.doribottle.domain.common

import org.springframework.security.acls.domain.ObjectIdentityImpl
import java.util.UUID

interface AclEntity {
    fun getId(): UUID
    fun toOi(): ObjectIdentityImpl {
        return ObjectIdentityImpl(this::class.java, this.getId())
    }
}