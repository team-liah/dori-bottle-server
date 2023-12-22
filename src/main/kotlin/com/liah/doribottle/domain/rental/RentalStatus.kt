package com.liah.doribottle.domain.rental

enum class RentalStatus(
    val title: String
) {
    PROCEEDING("진행 중"),
    CONFIRMED("확정"),
    SUCCEEDED("완료"),
    FAILED("실패"),
    CANCELED("취소")
}