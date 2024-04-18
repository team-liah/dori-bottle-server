package com.liah.doribottle.domain.machine

enum class MachineState(
    val title: String,
) {
    NORMAL("정상"),
    PAUSE("일시중지"),
    MALFUNCTION("고장"),
}
