package com.liah.doribottle.service.board

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.board.Board
import com.liah.doribottle.domain.board.BoardType.NOTICE
import com.liah.doribottle.repository.board.BoardRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull

class BoardServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var boardService: BoardService
    @Autowired
    private lateinit var boardRepository: BoardRepository

    @DisplayName("게시판 생성")
    @Test
    fun create() {
        //given, when
        val id = boardService.create("공지사항", "공지사항 게시판", NOTICE)
        clear()

        //then
        val findBoard = boardRepository.findByIdOrNull(id)
        assertThat(findBoard?.name).isEqualTo("공지사항")
        assertThat(findBoard?.description).isEqualTo("공지사항 게시판")
        assertThat(findBoard?.type).isEqualTo(NOTICE)
    }

    @DisplayName("게시판 생성 예외")
    @Test
    fun createException() {
        //given
        boardRepository.save(Board("공지사항", "공지사항 게시판", NOTICE))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            boardService.create("공지사항", "공지사항 게시판", NOTICE)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.BOARD_ALREADY_CREATED)
    }


}