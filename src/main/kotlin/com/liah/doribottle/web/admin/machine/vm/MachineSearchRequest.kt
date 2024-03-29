package com.liah.doribottle.web.admin.machine.vm

import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType

data class MachineSearchRequest(
    val no: String?,
    val name: String?,
    val type: MachineType?,
    val state: MachineState?,
    val addressKeyword: String?
)
