package com.liah.doribottle.service.post.dto

import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.service.account.dto.AdminDto
import com.liah.doribottle.web.admin.post.vm.PostDetailSearchResponse
import com.liah.doribottle.web.v1.post.vm.PostSearchResponse

data class PostDto(
    val author: AdminDto,
    val type: PostType,
    val title: String,
    val content: String
) {
    fun toSearchResponse() = PostSearchResponse(type, title, content)
    fun toDetailSearchResponse() = PostDetailSearchResponse(author, type, title, content)
}