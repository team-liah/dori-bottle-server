package com.liah.doribottle.service.board.dto

import com.liah.doribottle.domain.board.BoardType
import java.util.*

data class BoardDto(
    val id: UUID,
    val name: String,
    val description: String,
    val type: BoardType
)