package com.liah.doribottle.domain.payment

enum class PaymentStatus(
    val title: String
) {
    PROCEEDING("진행 중"),
    SUCCEEDED("완료"),
    FAILED("실패"),
    CANCELED("취소됨")
}