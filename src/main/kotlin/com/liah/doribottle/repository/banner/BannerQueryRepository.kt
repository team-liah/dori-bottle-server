package com.liah.doribottle.repository.banner

import com.liah.doribottle.domain.banner.Banner
import com.liah.doribottle.domain.banner.QBanner.Companion.banner
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class BannerQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        title: String? = null,
        header: String? = null,
        content: String? = null,
        visible: Boolean? = null,
        pageable: Pageable
    ): Page<Banner> {
        return queryFactory
            .selectFrom(banner)
            .where(
                titleContains(title),
                contentContains(content),
                visibleEq(visible)
            )
            .orderBy(banner.priority.asc())
            .toPage(pageable)
    }

    fun getAll(
        title: String? = null,
        header: String? = null,
        content: String? = null,
        visible: Boolean? = null
    ): List<Banner> {
        return queryFactory
            .selectFrom(banner)
            .where(
                titleContains(title),
                contentContains(content),
                visibleEq(visible)
            )
            .orderBy(banner.priority.asc())
            .fetch()
    }

    private fun titleContains(title: String?) = title?.let { banner.title.contains(it) }
    private fun headerContains(header: String?) = header?.let { banner.header.contains(it) }
    private fun contentContains(content: String?) = content?.let { banner.content.contains(it) }
    private fun visibleEq(visible: Boolean?) = visible?.let { banner.visible.eq(it) }
}