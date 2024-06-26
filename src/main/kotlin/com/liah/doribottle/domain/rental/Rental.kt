package com.liah.doribottle.domain.rental

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.rental.RentalStatus.*
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.generateRandomString
import com.liah.doribottle.service.rental.dto.RentalDto
import jakarta.persistence.*
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "rental",
    indexes = [
        Index(name = "INDEX_RENTAL_EXPIRED_DATE", columnList = "expiredDate"),
        Index(name = "INDEX_RENTAL_USER_ID", columnList = "user_id"),
        Index(name = "INDEX_RENTAL_CUP_ID", columnList = "cup_id"),
    ],
)
class Rental(
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cup_id", nullable = false)
    val cup: Cup,
    fromMachine: Machine,
    @Column(nullable = false)
    val withIce: Boolean,
    hourLimit: Long,
) : PrimaryKeyEntity() {
    @Column(nullable = false)
    val no: String = generateRandomString(8)

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_machine_id", nullable = false)
    val fromMachine: Machine =
        if (fromMachine.type == MachineType.VENDING) {
            fromMachine
        } else {
            throw IllegalArgumentException("Non VendingMachine is not allowed.")
        }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_machine_id")
    var toMachine: Machine? = null
        protected set

    @Column(nullable = false)
    var cost: Long = if (this.withIce) fromMachine.rentIceCupAmounts!! else fromMachine.rentCupAmounts!!
        protected set

    @Column
    var succeededDate: Instant? = null
        protected set

    @Column(nullable = false)
    var expiredDate: Instant = Instant.now().plus(hourLimit, ChronoUnit.HOURS)
        protected set

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RentalStatus = CONFIRMED
        protected set

    init {
        this.cup.loan()
        this.fromMachine.increaseCupAmounts(-1)
    }

    fun `return`(toMachine: Machine) {
        if (toMachine.type != MachineType.COLLECTION) {
            throw IllegalArgumentException("Non collectionMachine is not allowed.")
        }
        this.toMachine?.increaseCupAmounts(-1)
        toMachine.increaseCupAmounts(1)
        this.toMachine = toMachine

        if (this.status == CONFIRMED) {
            succeed()
        }

        cup.`return`()
    }

    private fun succeed() {
        this.status = SUCCEEDED
        this.succeededDate = Instant.now()
    }

    fun fail() {
        if (this.status == SUCCEEDED) {
            throw IllegalArgumentException("Cup return has already been succeeded.")
        }

        this.status = FAILED
        cup.lost()
    }

    fun cancel() {
        if (toMachine == null) {
            throw BusinessException(ErrorCode.RENTAL_CANCEL_NOT_ALLOWED)
        }

        this.status = CANCELED
    }

    fun toDto() =
        RentalDto(id, no, user.toSimpleDto(), cup.toDto(), fromMachine.toDto(), toMachine?.toDto(), withIce, cost, succeededDate, expiredDate, status, createdDate, lastModifiedDate)
}
