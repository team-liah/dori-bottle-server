package com.liah.doribottle.web.v1.post.vm

import com.liah.doribottle.domain.post.PostType

data class PostSearchResponse(
    val type: PostType,
    val title: String,
    val content: String
)