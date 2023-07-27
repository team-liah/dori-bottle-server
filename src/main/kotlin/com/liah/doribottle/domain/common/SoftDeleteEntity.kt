package com.liah.doribottle.domain.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass

@MappedSuperclass
abstract class SoftDeleteEntity : PrimaryKeyEntity() {
    @Column(nullable = false)
    var deleted: Boolean = false
        protected set

    fun delete() {
        this.deleted = true
    }
}