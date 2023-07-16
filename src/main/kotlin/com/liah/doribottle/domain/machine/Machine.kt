package com.liah.doribottle.domain.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.machine.MachineState.INITIAL
import com.liah.doribottle.domain.machine.MachineType.*
import com.liah.doribottle.service.machine.dto.MachineDto
import jakarta.persistence.*

@Entity
@Table(
    name = "machine",
    indexes = [Index(name = "IDX_MACHINE_NO", columnList = "no")]
)
class Machine(
    no: String,
    type: MachineType,
    address: Address,
    capacity: Long
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val no: String = no

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MachineType = type

    @Embedded
    var address: Address = address
        protected set

    @Column(nullable = false)
    var capacity: Long = capacity
        protected set

    @Column(nullable = false)
    var cupAmounts: Int = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: MachineState = INITIAL
        protected set

    fun increaseCupAmounts(amounts: Int) {
        val result = cupAmounts + amounts
        if (result > capacity) throw BusinessException(ErrorCode.FULL_OF_CUP)
        if (result < 0) throw BusinessException(ErrorCode.LACK_OF_CUP)

        cupAmounts = result
    }

    fun toDto() = MachineDto(id, no, type, address, capacity, cupAmounts, state)
}