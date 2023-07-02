package com.liah.doribottle.domain.user

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType.LAZY
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity
class RefreshToken(
    user: User
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = LAZY, optional = false)
    @JoinColumn(nullable = false)
    val user: User = user

    @Column(nullable = false)
    var token: String = UUID.randomUUID().toString()
        protected set

    @Column(nullable = false)
    var expiredDate: Instant = Instant.now().plus(14, ChronoUnit.DAYS)
        protected set

    fun refresh(millis: Long) {
        this.token = UUID.randomUUID().toString()
        this.expiredDate = Instant.now().plusMillis(millis)
    }
}