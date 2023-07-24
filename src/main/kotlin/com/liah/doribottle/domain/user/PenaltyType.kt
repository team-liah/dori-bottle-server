package com.liah.doribottle.domain.user

enum class PenaltyType(
    val title: String
) {
    DAMAGED_CUP("파손된 컵 반납"),
    NON_MANNER("비매너 행동"),
    ETC("기타")
}