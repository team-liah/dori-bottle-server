package com.liah.doribottle.service.board

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.board.Board
import com.liah.doribottle.domain.board.BoardType
import com.liah.doribottle.domain.board.Post
import com.liah.doribottle.repository.board.BoardRepository
import com.liah.doribottle.repository.board.PostQueryRepository
import com.liah.doribottle.repository.board.PostRepository
import com.liah.doribottle.service.board.dto.AuthorDto
import com.liah.doribottle.service.board.dto.BoardDto
import com.liah.doribottle.service.board.dto.PostDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class BoardService(
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val postQueryRepository: PostQueryRepository
) {
    fun create(
        name: String,
        description: String,
        type: BoardType
    ): UUID {
        verifyDuplicatedType(type)

        val board = boardRepository.save(
            Board(
                name = name,
                description = description,
                type = type
            )
        )

        return board.id
    }

    private fun verifyDuplicatedType(type: BoardType) {
        val existingBoard = boardRepository.findByType(type)
        if (existingBoard != null) {
            throw BusinessException(ErrorCode.BOARD_ALREADY_CREATED)
        }
    }

    @Transactional(readOnly = true)
    fun getAll(): List<BoardDto> {
        return boardRepository.findAll()
            .map { it.toDto() }
    }

    fun remove(id: UUID) {
        val board = boardRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.BOARD_NOT_FOUND)

        postRepository.deleteAllByBoard(board)
        boardRepository.delete(board)
    }

    fun registerPost(
        authorDto: AuthorDto,
        type: BoardType,
        title: String,
        content: String,
        notify: Boolean
    ): UUID {
        val board = boardRepository.findByType(type)
            ?: throw NotFoundException(ErrorCode.BOARD_NOT_FOUND)

        val post = postRepository.save(
            Post(
                author = authorDto.toEmbeddable(),
                board = board,
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
    fun getAllPosts(
        boardType: BoardType? = null,
        keyword: String? = null,
        pageable: Pageable
    ): Page<PostDto> {
        return postQueryRepository.getAll(
            boardType = boardType,
            keyword = keyword,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getPost(
        postId: UUID
    ): PostDto {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        return post.toDto()
    }

    fun updatePost(
        postId: UUID,
        title: String,
        content: String,
        notify: Boolean
    ) {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        val notifyToUsers = notify && !post.notify
        if (notifyToUsers) {
            // TODO: Notify to users
        }

        post.update(
            title = title,
            content = content,
            notify = notify
        )
    }

    fun removePost(
        postId: UUID
    ) {
        val post = postRepository.findByIdOrNull(postId)
            ?: throw NotFoundException(ErrorCode.POST_NOT_FOUND)

        postRepository.delete(post)
    }
}