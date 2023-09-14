package com.liah.doribottle.web.admin.post.vm

import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.service.account.dto.AdminDto

data class PostDetailSearchResponse(
    val author: AdminDto,
    val type: PostType,
    val title: String,
    val content: String
)