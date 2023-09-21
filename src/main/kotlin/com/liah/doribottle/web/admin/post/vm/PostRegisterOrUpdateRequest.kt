package com.liah.doribottle.web.admin.post.vm

import com.liah.doribottle.domain.post.PostType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class PostRegisterOrUpdateRequest(
    @field:NotNull
    val type: PostType?,
    @field:NotBlank
    val title: String?,
    @field:NotBlank
    val content: String?
)