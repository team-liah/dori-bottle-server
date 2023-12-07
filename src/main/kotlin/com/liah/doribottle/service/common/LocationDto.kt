package com.liah.doribottle.service.common

import com.liah.doribottle.domain.common.Location
import com.querydsl.core.annotations.QueryProjection

data class LocationDto @QueryProjection constructor(
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun toEmbeddable() = Location(latitude, longitude)
}