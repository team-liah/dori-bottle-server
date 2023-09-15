package com.liah.doribottle.domain.notification

enum class NotificationType(
    val title: String
) {
    POINT("버블 충전"),
    REFUND("환불 완료"),
    PENALTY("레드카드"),
    LOST_CUP("분실 처리"),
    AUTO_PAYMENT("자동결제"),
    NEAR_EXPIRATION("컵 반납 임박")
}