package com.liah.doribottle.repository.post

import com.liah.doribottle.domain.post.Post
import com.liah.doribottle.domain.post.PostType
import com.liah.doribottle.domain.post.QPost.Companion.post
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
        type: PostType? = null,
        keyword: String? = null,
        pageable: Pageable
    ): Page<Post> {
        return queryFactory
            .selectFrom(post)
            .where(
                typeEq(type),
                keywordContains(keyword)
            )
            .toPage(pageable)
    }

    private fun typeEq(type: PostType?) = type?.let { post.type.eq(it) }
    private fun keywordContains(keyword: String?) = keyword?.let { post.title.contains(it).or(post.content.contains(it)) }
}