package com.liah.doribottle.web.admin.post

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.post.PostService
import com.liah.doribottle.service.post.dto.PostDto
import com.liah.doribottle.web.admin.post.vm.PostRegisterOrUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/post")
class PostResource(
    private val postService: PostService
) {
    @Operation(summary = "게시글 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: PostRegisterOrUpdateRequest
    ): UUID {
        return postService.register(
            authorId = currentUserId()!!,
            type = request.type!!,
            title = request.title!!,
            content = request.content!!
        )
    }

    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    fun getAll(
        @RequestParam(value = "authorId", required = false) authorId: UUID?,
        @RequestParam(value = "type", required = false) type: PostType?,
        @RequestParam(value = "keyword", required = false) keyword: String?,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PostDto> {
        val result = postService.getAll(
            authorId = authorId,
            type = type,
            keyword = keyword,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "게시글 조회")
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID) = postService.get(id)

    @Operation(summary = "게시글 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: PostRegisterOrUpdateRequest
    ) {
        postService.update(
            id = id,
            type = request.type!!,
            title = request.title!!,
            content = request.content!!
        )
    }

    @Operation(summary = "게시글 삭제")
    @DeleteMapping("/{id}")
    fun remove(
        @PathVariable id: UUID
    ) {
        postService.remove(id)
    }
}