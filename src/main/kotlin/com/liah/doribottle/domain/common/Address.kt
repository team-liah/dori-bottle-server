package com.liah.doribottle.domain.common

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    @Column(nullable = false)
    val zipCode: String,

    @Column(nullable = false)
    val address1: String,

    @Column
    val address2: String? = null
)