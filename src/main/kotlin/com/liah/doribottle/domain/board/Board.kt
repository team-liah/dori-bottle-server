package com.liah.doribottle.domain.board

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.service.board.dto.BoardDto
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "board",
    indexes = [Index(name = "IDX_BOARD_TYPE", columnList = "type", unique = true)]
)
class Board(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val description: String,

    @Column(nullable = false, unique = true)
    val type: String
) : PrimaryKeyEntity() {
    fun toDto() = BoardDto(id, name, description, type)
}