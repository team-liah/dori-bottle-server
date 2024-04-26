package com.liah.doribottle.domain.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.service.machine.dto.MachineSimpleDto
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MachineType,

    address: Address,

    location: Location,

    capacity: Int,

    imageUrl: String? = null
) : SoftDeleteEntity() {
    @Column(nullable = false, unique = true)
    var no: String = no
        protected set

    @Column(nullable = false)
    var name: String = name
        protected set

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

    @Column
    var imageUrl: String? = imageUrl
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
        state: MachineState,
        imageUrl: String?
    ) {
        this.name = name
        this.address = address
        this.location = location
        this.capacity = capacity
        this.state = state
        this.imageUrl = imageUrl
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

    fun toDto() = MachineDto(id, no, name, type, address.toDto(), location.toDto(), capacity, cupAmounts, state, imageUrl, createdDate, lastModifiedDate)
    fun toSimpleDto() = MachineSimpleDto(id, type, location.toDto(), state)
}