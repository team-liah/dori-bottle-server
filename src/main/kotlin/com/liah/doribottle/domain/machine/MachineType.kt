package com.liah.doribottle.domain.machine

enum class MachineType(
    val title: String
) {
    VENDING("자판기"),
    COLLECTION("수거함"),
    WASHING("세척함")
}