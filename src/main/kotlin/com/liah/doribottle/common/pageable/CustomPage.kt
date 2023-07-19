package com.liah.doribottle.common.pageable

import org.springframework.data.domain.Page

data class CustomPage<T>(
    val content: List<T>,
    val pageable: CustomPageable
) {
    companion object {
        fun <T> of(page: Page<T>): CustomPage<T> {
            return CustomPage(
                content = page.content,
                pageable = CustomPageable(
                    first = page.isFirst,
                    last = page.isLast,
                    hasNext = page.hasNext(),
                    page = page.number,
                    totalPages = page.totalPages,
                    totalElements = page.totalElements
                )
            )
        }
    }
}