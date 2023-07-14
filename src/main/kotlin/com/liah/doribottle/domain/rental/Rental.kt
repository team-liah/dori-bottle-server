package com.liah.doribottle.domain.rental

import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.cup.Cup
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.rental.RentalStatus.PROCEEDING
import com.liah.doribottle.domain.user.User
import jakarta.persistence.*
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@Table(
    name = "rental",
    indexes = [
        Index(name = "INDEX_RENTAL_USER_ID", columnList = "user_id"),
        Index(name = "INDEX_RENTAL_CUP_ID", columnList = "cup_id")
    ]
)
class Rental(
    user: User,
    cup: Cup,
    fromMachine: Machine,
    withIce: Boolean,
    dayLimit: Long
) : PrimaryKeyEntity() {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = user

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cup_id", nullable = false)
    val cup: Cup = cup

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_machine_id", nullable = false)
    val fromMachine: Machine =
        if (fromMachine.type == MachineType.VENDING) fromMachine
        else throw IllegalArgumentException("Non VendingMachine is not allowed.")

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_machine_id")
    var toMachine: Machine? = null
        protected set

    @Column(nullable = false)
    val withIce: Boolean = withIce

    @Column(nullable = false)
    var cost: Long = if (withIce) 2 else 1
        protected set

    @Column
    var succeededDate: Instant? = null
        protected set

    @Column
    var expiredDate: Instant = Instant.now().plus(dayLimit, ChronoUnit.DAYS)
        protected set

    @Column(nullable = false)
    var status: RentalStatus = PROCEEDING
        protected set
}