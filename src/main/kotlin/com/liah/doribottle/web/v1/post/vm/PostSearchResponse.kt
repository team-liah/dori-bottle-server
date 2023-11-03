package com.liah.doribottle.web.v1.post.vm

import com.liah.doribottle.domain.post.PostType
import java.time.Instant
import java.util.*

data class PostSearchResponse(
    val id: UUID,
    val type: PostType,
    val title: String,
    val content: String,
    val createdDate: Instant,
    val lastModifiedDate: Instant
)