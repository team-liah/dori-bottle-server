package com.liah.doribottle.domain.machine

enum class MachineState(
    val title: String
) {
    INITIAL("초기 등록"),
    PENDING("보류"),
    NORMAL("정상"),
    MALFUNCTION("고장")
}