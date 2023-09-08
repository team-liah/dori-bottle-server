package com.liah.doribottle.domain.point

enum class PointEventType(
    val title: String
) {
    SAVE_REGISTER_REWARD("회원가입 보상"),
    SAVE_REGISTER_INVITER_REWARD("초대코드 입력 보상"),
    SAVE_INVITE_REWARD("친구초대 보상"),
    SAVE_PAY("충전"),
    CANCEL_SAVE("적립 취소"),

    USE_CUP("컵 사용"),

    DISAPPEAR("소멸")
}