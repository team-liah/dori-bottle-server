package com.liah.doribottle.service.board

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.board.Board
import com.liah.doribottle.domain.board.BoardType
import com.liah.doribottle.repository.board.BoardRepository
import com.liah.doribottle.service.board.dto.BoardDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class BoardService(
    private val boardRepository: BoardRepository
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
        //TODO: Remove Posts
        boardRepository.deleteById(id)
    }
}