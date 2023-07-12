package com.liah.doribottle.domain.machine

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.PrimaryKeyEntity
import com.liah.doribottle.domain.cup.Cup
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
    capacity: Long,
    state: MachineState
) : PrimaryKeyEntity() {
    @Column(nullable = false, unique = true)
    val no: String = no

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: MachineType = type

    @Column(nullable = false)
    var address: Address = address
        protected set

    @Column(nullable = false)
    var capacity: Long = capacity
        protected set

    @Column(nullable = false)
    var state: MachineState = state
        protected set

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "machine")
    protected val mutableCups: MutableSet<Cup> = mutableSetOf()
    val cups: Set<Cup> get() = mutableCups
}