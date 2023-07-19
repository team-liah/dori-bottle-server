package com.liah.doribottle.domain.cup

enum class CupStatus(
    val title: String
) {
    AVAILABLE("이용 가능"),
    ON_LOAN("대여 중"),
    RETURNED("반납됨"),
    WASHING("세척 중"),
    LOST("분실됨"),
    DAMAGED("손상됨")
}