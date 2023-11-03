package com.liah.doribottle.service.post.dto

import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.service.account.dto.AdminDto
import com.liah.doribottle.web.v1.post.vm.PostSearchResponse
import java.time.Instant
import java.util.*

data class PostDto(
    val id: UUID,
    val author: AdminDto,
    val type: PostType,
    val title: String,
    val content: String,
    val createdDate: Instant,
    val lastModifiedDate: Instant
) {
    fun toSearchResponse() = PostSearchResponse(id, type, title, content, createdDate, lastModifiedDate)
}