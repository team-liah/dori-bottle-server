package com.liah.doribottle.domain.task

enum class TaskType(
    val title: String
) {
    RENTAL_OVERDUE("대여 반납 기간 초과"), RENTAL_REMIND("대여 반납 리마인드")
}