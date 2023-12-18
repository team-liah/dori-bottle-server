package com.liah.doribottle.domain.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.AclEntity
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.service.machine.dto.MachineDetailDto
import com.liah.doribottle.service.machine.dto.MachineDto
import jakarta.persistence.*
import org.slf4j.LoggerFactory
import java.util.*

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
    location: Location,
    capacity: Int
) : AclEntity, SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    var no: String = no

    @Column(nullable = false)
    var name: String = name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MachineType = type

    @Embedded
    var address: Address = address
        protected set

    @Embedded
    var location: Location = location
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

    override fun delete() {
        this.no = "Deleted ${UUID.randomUUID()}"
        super.delete()
    }

    fun update(
        name: String,
        address: Address,
        location: Location,
        capacity: Int,
        cupAmounts: Int,
        state: MachineState
    ) {
        this.name = name
        this.address = address
        this.location = location
        this.capacity = capacity
        this.state = state
        updateCupAmounts(cupAmounts)
    }

    fun increaseCupAmounts(amounts: Int) {
        updateCupAmounts(cupAmounts + amounts)
    }

    fun updateCupAmounts(amounts: Int) {
        runCatching {
            if (amounts > capacity) {
                cupAmounts = capacity
                throw BusinessException(ErrorCode.FULL_OF_CUP)
            } else if (amounts < 0) {
                cupAmounts = 0
                throw BusinessException(ErrorCode.LACK_OF_CUP)
            } else {
                cupAmounts = amounts
            }
        }.onFailure {
            val log = LoggerFactory.getLogger(javaClass)
            log.error(it.message)
        }
    }

    fun toDto() = MachineDto(id, no, name, type, address.toDto(), location.toDto(), capacity, cupAmounts, state, deleted, createdDate, lastModifiedDate)
    fun toDetailDto(managers: List<User>, managementGroups: List<Group>) = MachineDetailDto(id, no, name, type, address.toDto(), location.toDto(), capacity, cupAmounts, state, managers.map { it.toSimpleDto() }, managementGroups.map { it.toDto() }, deleted, createdDate, lastModifiedDate)
}