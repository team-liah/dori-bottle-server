package com.liah.doribottle.web.admin.banner.vm

data class BannerPatchRequest(
    val title: String? = null,
    val header: String? = null,
    val content: String? = null,
    val priority: Int? = null,
    val visible: Boolean? = null,
    val backgroundColor: String? = null,
    val backgroundImageUrl: String? = null,
    val imageUrl: String? = null,
    val targetUrl: String? = null
)