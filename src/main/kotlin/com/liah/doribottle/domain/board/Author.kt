package com.liah.doribottle.domain.board

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.service.board.dto.AuthorDto
import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import java.util.*

@Embeddable
data class Author(
    @Column(name = "author_id", nullable = false)
    val id: UUID,

    @Column(name = "author_role", nullable = false)
    val role: Role
) {
    fun toDto() = AuthorDto(id, role)
}