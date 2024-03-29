package com.liah.doribottle.service.banner.dto

import com.liah.doribottle.web.v1.banner.vm.BannerSearchResponse
import java.time.Instant
import java.util.*

data class BannerDto(
    val id: UUID,
    val title: String,
    val header: String?,
    val content: String?,
    val priority: Int,
    val visible: Boolean,
    val backgroundColor: String?,
    val backgroundImageUrl: String?,
    val imageUrl: String?,
    val targetUrl: String?,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toSearchResponse(): BannerSearchResponse {
        return BannerSearchResponse(id, header, content, priority, visible, backgroundColor, backgroundImageUrl, imageUrl, targetUrl)
    }
}