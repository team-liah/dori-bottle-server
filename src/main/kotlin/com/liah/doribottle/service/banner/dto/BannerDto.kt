package com.liah.doribottle.service.banner.dto

import java.time.Instant
import java.util.*

data class BannerDto(
    val id: UUID,
    val title: String,
    val content: String,
    val priority: Int,
    val visible: Boolean,
    val backgroundColor: String?,
    val imageUrl: String?,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)
