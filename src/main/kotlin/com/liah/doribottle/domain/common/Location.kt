package com.liah.doribottle.domain.common

import com.liah.doribottle.service.common.LocationDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
data class Location(
    @Column
    val latitude: Double? = null,

    @Column
    val longitude: Double? = null,
) {
    fun toDto() = LocationDto(latitude, longitude)
}