package com.liah.doribottle.domain.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.machine.MachineState.NORMAL
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
    name: String,
    type: MachineType,
    address: Address,
    capacity: Int
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val no: String = no

    @Column(nullable = false)
    var name: String = name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MachineType = type

    @Embedded
    var address: Address = address
        protected set

    @Column(nullable = false)
    var capacity: Int = capacity
        protected set

    @Column(nullable = false)
    var cupAmounts: Int = 0
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: MachineState = NORMAL
        protected set

    fun update(
        name: String,
        address: Address,
        capacity: Int
    ) {
        this.name = name
        this.address = address
        this.capacity = capacity
    }

    fun increaseCupAmounts(amounts: Int) {
        updateCupAmounts(cupAmounts + amounts)
    }

    fun updateCupAmounts(amounts: Int) {
        if (amounts > capacity) throw BusinessException(ErrorCode.FULL_OF_CUP)
        if (amounts < 0) throw BusinessException(ErrorCode.LACK_OF_CUP)

        cupAmounts = amounts
    }

    fun toDto() = MachineDto(id, no, name, type, address, capacity, cupAmounts, state)
}