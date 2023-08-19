package com.liah.doribottle.domain.payment.card

enum class CardType(
    val title: String
) {
    CREDIT("신용"), CHECK("체크"), GIFT("기프트")
}