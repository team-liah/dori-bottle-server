package com.liah.doribottle.domain.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.machine.MachineState.INITIAL
import com.liah.doribottle.domain.machine.MachineType.*
import com.liah.doribottle.service.machine.dto.MachineDto
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(
    name = "machine",
    indexes = [Index(name = "IDX_MACHINE_NO", columnList = "no")]
)
@SQLDelete(sql = "UPDATE machine SET deleted = true where id = ?")
@Where(clause = "deleted = false")
class Machine(
    no: String,
    type: MachineType,
    address: Address,
    capacity: Int
) : SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    val no: String = no

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
    var state: MachineState = INITIAL
        protected set

    fun update(
        address: Address,
        capacity: Int
    ) {
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

    fun toDto() = MachineDto(id, no, type, address, capacity, cupAmounts, state)
}