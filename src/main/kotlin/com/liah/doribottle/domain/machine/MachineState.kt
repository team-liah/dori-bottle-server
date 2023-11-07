package com.liah.doribottle.domain.machine

enum class MachineState(
    val title: String
) {
    NORMAL("정상"),
    MALFUNCTION("고장")
}