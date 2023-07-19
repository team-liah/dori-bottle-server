package com.liah.doribottle.common.pageable

data class CustomPageable(
    val first: Boolean = false,
    val last: Boolean = false,
    val hasNext: Boolean = false,
    val page: Int = 0,
    val totalPages: Int = 0,
    val totalElements: Long = 0
)