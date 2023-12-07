package com.liah.doribottle.repository.machine

import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.machine.QMachine.Companion.machine
import com.liah.doribottle.extension.toPage
import com.liah.doribottle.service.common.QLocationDto
import com.liah.doribottle.service.machine.dto.MachineSimpleDto
import com.liah.doribottle.service.machine.dto.QMachineSimpleDto
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository

@Repository
class MachineQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    fun getAll(
        no: String? = null,
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<Machine> {
        return queryFactory
            .selectFrom(machine)
            .where(
                noContains(no),
                nameContains(name),
                typeEq(type),
                stateEq(state),
                addressKeywordContains(addressKeyword),
                deletedEq(deleted)
            )
            .toPage(pageable)
    }

    fun getAll(
        deleted: Boolean? = null
    ): List<MachineSimpleDto> {
        return queryFactory
            .select(
                QMachineSimpleDto(
                    machine.id,
                    machine.type,
                    QLocationDto(
                        machine.location.latitude,
                        machine.location.longitude
                    ),
                    machine.state
                )
            )
            .from(machine)
            .where(
                deletedEq(deleted)
            )
            .fetch()
    }

    private fun noContains(no: String?) = no?.let { machine.no.contains(it) }
    private fun nameContains(name: String?) = name?.let { machine.name.contains(it) }
    private fun typeEq(type: MachineType?) = type?.let { machine.type.eq(it) }
    private fun stateEq(state: MachineState?) = state?.let { machine.state.eq(it) }
    private fun addressKeywordContains(addressKeyword: String?) = addressKeyword?.let {
        machine.address.zipCode.contains(it)
            .or(machine.address.address1.contains(it))
            .or(machine.address.address2.contains(it))
    }
    private fun deletedEq(deleted: Boolean?) = deleted?.let { machine.deleted.eq(it) }
}