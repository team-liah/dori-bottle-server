package com.liah.doribottle.service.common

import com.liah.doribottle.domain.common.Address

data class AddressDto(
    val zipCode: String? = null,
    val address1: String? = null,
    val address2: String? = null,
) {
    fun toEmbeddable() = Address(zipCode, address1, address2)
}