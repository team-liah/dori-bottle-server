package com.liah.doribottle.web.v1.post

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.service.post.PostService
import com.liah.doribottle.web.v1.post.vm.PostSearchResponse
import io.swagger.v3.oas.annotations.Operation
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/post")
class PostController(
    private val postService: PostService
) {
    @Operation(summary = "게시글 목록 조회")
    @GetMapping
    fun getAll(
        @RequestParam(value = "type", required = true) type: PostType,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<PostSearchResponse> {
        val result = postService.getAll(
            type = type,
            pageable = pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

    @Operation(summary = "게시글 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ) = postService.get(id).toSearchResponse()
}