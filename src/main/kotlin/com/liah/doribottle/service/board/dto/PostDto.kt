package com.liah.doribottle.service.board.dto

data class PostDto(
    val author: AuthorDto,
    val boardType: String,
    val title: String,
    val content: String,
    val notify: Boolean
)