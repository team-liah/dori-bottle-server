package com.liah.doribottle.repository.machine

import com.liah.doribottle.domain.machine.Machine
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface MachineRepository : JpaRepository<Machine, UUID> {
    fun findByNo(no: String): Machine?
}