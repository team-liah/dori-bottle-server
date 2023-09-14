package com.liah.doribottle.service.post

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.post.Post
import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.repository.post.PostQueryRepository
import com.liah.doribottle.repository.post.PostRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.post.dto.PostDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class PostService(
    private val postRepository: PostRepository,
    private val postQueryRepository: PostQueryRepository,
    private val adminRepository: AdminRepository
) {
    fun register(
        authorId: UUID,
        type: PostType,
        title: String,
        content: String,
        notify: Boolean
    ): UUID {
        val author = adminRepository.findByIdOrNull(authorId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        val post = postRepository.save(
            Post(
                author = author,
                type = type,
                title = title,
                content = content,
                notify = notify
            )
        )

        if (notify) {
            // TODO: Notify to users
        }

        return post.id
    }

    @Transactional(readOnly = true)
    fun getAll(
        type: PostType? = null,
        keyword: String? = null,
        pageable: Pageable
    ): Page<PostDto> {
        return postQueryRepository.getAll(
            type = type,
            keyword = keyword,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun get(
        id: UUID
    ): PostDto {
        val post = postRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        return post.toDto()
    }

    fun update(
        id: UUID,
        type: PostType,
        title: String,
        content: String,
        notify: Boolean
    ) {
        val post = postRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        val notifyToUsers = notify && !post.notify
        if (notifyToUsers) {
            // TODO: Notify to users
        }

        post.update(
            type = type,
            title = title,
            content = content,
            notify = notify
        )
    }

    fun remove(
        id: UUID
    ) {
        val post = postRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        postRepository.delete(post)
    }
}