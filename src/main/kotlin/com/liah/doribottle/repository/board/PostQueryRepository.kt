package com.liah.doribottle.repository.board

import com.liah.doribottle.domain.board.Post
import com.liah.doribottle.domain.board.QPost.Companion.post
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class PostQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        boardType: String? = null,
        keyword: String? = null,
        pageable: Pageable
    ): Page<Post> {
        return queryFactory
            .selectFrom(post)
            .where(
                boardTypeEq(boardType),
                keywordContains(keyword)
            )
            .toPage(pageable)
    }

    private fun boardTypeEq(boardType: String?) = boardType?.let { post.board.type.eq(it) }
    private fun keywordContains(keyword: String?) = keyword?.let { post.title.contains(it).or(post.content.contains(it)) }
}