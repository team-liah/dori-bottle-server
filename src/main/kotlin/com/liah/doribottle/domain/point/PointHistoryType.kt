package com.liah.doribottle.domain.point

enum class PointHistoryType(
    val title: String
) {
    SAVE_REGISTER_REWARD("회원가입 보상 적립"),
    SAVE_INVITE_REWARD("초대 보상 적립"),
    SAVE_PAY("충전 적립"),
    CANCEL_SAVE("적립 취소"),

    USE_CUP("컵 사용"),
    CANCEL_USE("컵 사용 취소"),

    DISAPPEAR("소멸")
}