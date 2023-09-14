package com.liah.doribottle.service.post.dto

import com.liah.doribottle.domain.post.PostType
import java.util.*

data class PostDto(
    val authorId: UUID,
    val type: PostType,
    val title: String,
    val content: String,
    val notify: Boolean
)