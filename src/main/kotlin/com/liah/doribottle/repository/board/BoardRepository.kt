package com.liah.doribottle.repository.board

import com.liah.doribottle.domain.board.Board
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BoardRepository : JpaRepository<Board, UUID> {
    fun findByType(type: String): Board?
}