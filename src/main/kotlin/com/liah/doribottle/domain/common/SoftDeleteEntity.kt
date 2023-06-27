package com.liah.doribottle.domain.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.Instant

@MappedSuperclass
abstract class SoftDeleteEntity : PrimaryKeyEntity() {
    @Column
    var deletedReason: String? = null
        protected set

    @Column
    var deletedDate: Instant? = null
        protected set

    @Column
    var deletedBy: String? = null
        protected set
}