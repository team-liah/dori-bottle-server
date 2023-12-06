package com.liah.doribottle.domain.task

enum class TaskType(
    val title: String
) {
    RENTAL_RETURN("대여 반납 처리"), RENTAL_REMIND("대여 리마인드")
}