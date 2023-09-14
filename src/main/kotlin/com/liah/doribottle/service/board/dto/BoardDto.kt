package com.liah.doribottle.service.board.dto

import java.util.*

data class BoardDto(
    val id: UUID,
    val name: String,
    val description: String,
    val type: String
)