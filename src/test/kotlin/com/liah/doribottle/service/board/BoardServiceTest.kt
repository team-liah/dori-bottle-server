package com.liah.doribottle.service.board

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.board.Board
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.board.BoardRepository
import com.liah.doribottle.repository.board.PostRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.board.dto.AuthorDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class BoardServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var boardService: BoardService
    @Autowired
    private lateinit var boardRepository: BoardRepository
    @Autowired
    private lateinit var postRepository: PostRepository

    @DisplayName("게시판 생성")
    @Test
    fun create() {
        //given, when
        val id = boardService.create("공지사항", "공지사항 게시판", "NOTICE")
        clear()

        //then
        val findBoard = boardRepository.findByIdOrNull(id)
        assertThat(findBoard?.name).isEqualTo("공지사항")
        assertThat(findBoard?.description).isEqualTo("공지사항 게시판")
        assertThat(findBoard?.type).isEqualTo("NOTICE")
    }

    @DisplayName("게시판 생성 예외")
    @Test
    fun createException() {
        //given
        boardRepository.save(Board("공지사항", "공지사항 게시판", "NOTICE"))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            boardService.create("공지사항", "공지사항 게시판", "NOTICE")
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.BOARD_ALREADY_CREATED)
    }

    @DisplayName("게시판 목록 조회")
    @Test
    fun getAll() {
        //given
        boardRepository.save(Board("공지사항", "공지사항 게시판", "NOTICE"))
        boardRepository.save(Board("FAQ", "FAQ", "FAQ"))
        clear()

        //when
        val result = boardService.getAll()

        assertThat(result)
            .extracting("name")
            .containsExactly("공지사항", "FAQ")
        assertThat(result)
            .extracting("type")
            .containsExactly("NOTICE", "FAQ")
    }

    @DisplayName("게시판 삭제")
    @Test
    fun remove() {
        //given
        val board = boardRepository.save(Board("공지사항", "공지사항 게시판", "NOTICE"))
        clear()

        //when
        boardService.remove(board.id)
        clear()

        //then
        val findBoard = boardRepository.findByIdOrNull(board.id)
        assertThat(findBoard).isNull()
    }

    @DisplayName("게시글 등록")
    @Test
    fun registerPost() {
        //given
        boardRepository.save(Board("공지사항", "공지사항 게시판", "NOTICE"))
        val author = AuthorDto(UUID.randomUUID(), Role.ADMIN)
        clear()

        //when
        val postId = boardService.registerPost(author, "NOTICE", "공지글", "공지글 내용", false)
        clear()

        //then
        val findPost = postRepository.findByIdOrNull(postId)
        assertThat(findPost?.board?.type).isEqualTo("NOTICE")
        assertThat(findPost?.author).isEqualTo(author.toEmbeddable())
        assertThat(findPost?.title).isEqualTo("공지글")
        assertThat(findPost?.content).isEqualTo("공지글 내용")
        assertThat(findPost?.notify).isEqualTo(false)
    }
}