package com.liah.doribottle.repository.board

import com.liah.doribottle.domain.board.Board
import com.liah.doribottle.domain.board.Post
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PostRepository : JpaRepository<Post, UUID> {
    fun deleteAllByBoard(board: Board)
}