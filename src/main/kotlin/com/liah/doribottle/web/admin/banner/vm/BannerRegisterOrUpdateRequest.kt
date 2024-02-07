package com.liah.doribottle.web.admin.banner.vm

import jakarta.validation.constraints.NotNull

data class BannerRegisterOrUpdateRequest(
    @field:NotNull
    val title: String?,
    val header: String?,
    val content: String?,
    @field:NotNull
    val priority: Int?,
    @field:NotNull
    val visible: Boolean?,
    val backgroundColor: String?,
    val backgroundImageUrl: String?,
    val imageUrl: String?,
    val targetUrl: String?
)