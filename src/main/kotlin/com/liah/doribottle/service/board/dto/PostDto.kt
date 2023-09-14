package com.liah.doribottle.service.board.dto

import com.liah.doribottle.domain.board.BoardType

data class PostDto(
    val author: AuthorDto,
    val boardType: BoardType,
    val title: String,
    val content: String,
    val notify: Boolean
)
