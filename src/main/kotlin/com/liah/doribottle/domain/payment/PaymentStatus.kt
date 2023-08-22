package com.liah.doribottle.domain.payment

enum class PaymentStatus(
    val title: String
) {
    PROCEEDING("진행 중"),
    SUCCEEDED("완료"),
    FAILED("실패"),
    CANCEL_PROCEEDING("취소 진행 중"),
    CANCEL_SUCCEEDED("취소 완료"),
    CANCEL_FAILED("취소 실패")
}