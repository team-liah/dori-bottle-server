package com.liah.doribottle.repository.machine

import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.machine.QMachine.Companion.machine
import com.liah.doribottle.extension.toPage
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MachineQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        type: MachineType?,
        state: MachineState?,
        addressKeyword: String?,
        pageable: Pageable
    ): Page<Machine> {
        return queryFactory
            .selectFrom(machine)
            .where(
                typeEq(type),
                stateEq(state),
                addressKeywordContains(addressKeyword)
            )
            .toPage(pageable)
    }

    private fun typeEq(type: MachineType?) = type?.let { machine.type.eq(it) }
    private fun stateEq(state: MachineState?) = state?.let { machine.state.eq(it) }
    private fun addressKeywordContains(addressKeyword: String?) = addressKeyword?.let {
        machine.address.zipCode.contains(it)
            .or(machine.address.address1.contains(it))
            .or(machine.address.address2.contains(it))
    }
}