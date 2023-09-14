package com.liah.doribottle.service.board.dto

import com.liah.doribottle.domain.board.Author
import com.liah.doribottle.domain.user.Role
import java.util.*

data class AuthorDto(
    val id: UUID,
    val role: Role
) {
    fun toEmbeddable() = Author(id, role)
}