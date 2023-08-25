package com.liah.doribottle.domain.payment

enum class PaymentType(
    val title: String
) {
    SAVE_POINT("버블 충전"), LOST_CUP("컵 분실"), UNBLOCK_ACCOUNT("블락 해제")
}