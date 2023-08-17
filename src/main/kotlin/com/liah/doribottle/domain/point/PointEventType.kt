package com.liah.doribottle.domain.point

enum class PointEventType(
    val title: String
) {
    SAVE_REGISTER_REWARD("회원가입 보상 적립"),
    SAVE_REGISTER_INVITER_REWARD("초대코드 등록 보상 적립"),
    SAVE_INVITE_REWARD("초대 보상 적립"),
    SAVE_PAY("충전 적립"),
    CANCEL_SAVE("적립 취소"),

    USE_CUP("컵 사용"),

    DISAPPEAR("소멸")
}