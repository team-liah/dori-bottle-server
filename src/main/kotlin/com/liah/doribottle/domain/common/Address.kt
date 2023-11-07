package com.liah.doribottle.domain.common

import com.liah.doribottle.service.common.AddressDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Address(
    @Column
    val zipCode: String? = null,

    @Column
    val address1: String? = null,

    @Column
    val address2: String? = null
) {
    fun toDto() = AddressDto(zipCode, address1, address2)
}