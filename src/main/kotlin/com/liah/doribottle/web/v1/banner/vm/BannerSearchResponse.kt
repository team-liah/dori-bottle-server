package com.liah.doribottle.web.v1.banner.vm

import java.util.*

data class BannerSearchResponse(
    val id: UUID,
    val title: String,
    val content: String,
    val priority: Int,
    val visible: Boolean,
    val backgroundColor: String?,
    val imageUrl: String?,
    val targetUrl: String?
)