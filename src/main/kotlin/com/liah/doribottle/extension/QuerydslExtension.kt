package com.liah.doribottle.extension

import com.querydsl.core.types.Expression
import com.querydsl.core.types.Order
import com.querydsl.core.types.OrderSpecifier
import com.querydsl.core.types.dsl.PathBuilder
import com.querydsl.jpa.JPQLQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort

fun <T> JPQLQuery<T>.paging(pageable: Pageable): JPQLQuery<T> {
    if (pageable.isPaged) {
        val limit = pageable.pageSize.toLong()
        val offset = pageable.offset

        this.offset(offset)
        this.limit(limit)
    }

    return this
}

fun <T> JPQLQuery<T>.sorting(sort: Sort): JPQLQuery<T> {
    val projection = metadata.projection
    if (sort.isSorted && projection != null) {
        val orders =
            sort.map {
                val direction = if (it.isAscending) Order.ASC else Order.DESC
                val prop = it.property
                val orderByExpression = PathBuilder(projection.type, projection.toString())

                OrderSpecifier(direction, orderByExpression.get(prop) as Expression<Comparable<Any>>)
            }.toList().toTypedArray()

        this.orderBy(*orders)
    }

    return this
}

fun <T> JPQLQuery<T>.toPage(pageable: Pageable): Page<T> {
    val queryResults =
        this.paging(pageable)
            .sorting(pageable.sort)
            .fetchResults()

    return PageImpl(queryResults.results, pageable, queryResults.total)
}
