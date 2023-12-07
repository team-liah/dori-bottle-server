package com.liah.doribottle.service.common

import com.liah.doribottle.domain.common.Location

data class LocationDto(
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toEmbeddable() = Location(latitude, longitude)
}