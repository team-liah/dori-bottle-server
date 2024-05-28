package com.liah.doribottle.domain.machine

import com.liah.doribottle.apiclient.vm.SlackMessageType
import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.constant.DoriConstant
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.common.SoftDeleteEntity
import com.liah.doribottle.event.Events
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.service.machine.dto.MachineSimpleDto
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Index
import jakarta.persistence.Table
import org.slf4j.LoggerFactory
import java.util.UUID

@Entity
@Table(
    name = "machine",
    indexes = [Index(name = "IDX_MACHINE_NO", columnList = "no")],
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
    imageUrl: String? = null,
    rentCupAmounts: Long? = null,
    rentIceCupAmounts: Long? = null,
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
        protected set(value) {
            val notifyLackOfCup = field > value && this.type == MachineType.VENDING && field.toFloat() / this.capacity < 0.1
            val notifyFullOfCup = field < value && this.type == MachineType.COLLECTION && field.toFloat() / this.capacity > 0.9

            field = value

            if (notifyLackOfCup) {
                Events.notifySlack(SlackMessageType.MACHINE_LACK_OF_CUP, this.toDto())
            }
            if (notifyFullOfCup) {
                Events.notifySlack(SlackMessageType.MACHINE_FULL_OF_CUP, this.toDto())
            }
        }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var state: MachineState = MachineState.NORMAL
        protected set(value) {
            val notifyStateChange = field != value

            field = value

            if (notifyStateChange) {
                Events.notifySlack(SlackMessageType.MACHINE_STATE_CHANGE, this.toDto())
            }
        }

    @Column
    var imageUrl: String? = imageUrl
        protected set

    // 컵 대여에 필요한 포인트 개수
    @Column
    var rentCupAmounts: Long? = rentCupAmounts
        get() {
            return if (this.type != MachineType.VENDING) {
                null
            } else {
                field ?: DoriConstant.RENT_CUP_AMOUNTS
            }
        }
        protected set

    // 얼음컵 대여에 필요한 포인트 개수
    @Column
    var rentIceCupAmounts: Long? = rentIceCupAmounts
        get() {
            return if (this.type != MachineType.VENDING) {
                null
            } else {
                field ?: DoriConstant.RENT_ICE_CUP_AMOUNTS
            }
        }
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
        imageUrl: String? = null,
        rentCupAmounts: Long? = null,
        rentIceCupAmounts: Long? = null,
    ) {
        this.name = name
        this.address = address
        this.location = location
        this.capacity = capacity
        this.state = state
        this.imageUrl = imageUrl
        this.rentCupAmounts = rentCupAmounts
        this.rentIceCupAmounts = rentIceCupAmounts
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

    fun toDto() =
        MachineDto(
            id = id,
            no = no,
            name = name,
            type = type,
            address = address.toDto(),
            location = location.toDto(),
            capacity = capacity,
            cupAmounts = cupAmounts,
            state = state,
            imageUrl = imageUrl,
            rentCupAmounts = rentCupAmounts,
            rentIceCupAmounts = rentIceCupAmounts,
            createdDate = createdDate,
            lastModifiedDate = lastModifiedDate,
        )

    fun toSimpleDto() = MachineSimpleDto(id, type, location.toDto(), state)
}
