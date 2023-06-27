package com.liah.doribottle.domain.common

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class Address(
    @Column
    val zipCode: String?,

    @Column
    val address1: String?,

    @Column
    val address2: String?
)