package com.liah.doribottle.service.machine

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.domain.machine.MachineState.INITIAL
import com.liah.doribottle.domain.machine.MachineType
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class MachineServiceTest {
    @PersistenceContext
    private lateinit var entityManager: EntityManager
    @Autowired
    private lateinit var machineRepository: MachineRepository
    @Autowired
    private lateinit var machineService: MachineService

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("자판기 등록")
    @Test
    fun registerTest() {
        //given
        val no = "0000000"
        val address = Address("12345", "삼성로", null)

        //when
        val id = machineService.register(no, MachineType.VENDING, address, 100)
        clear()

        //then
        val machine = machineRepository.findByIdOrNull(id)
        assertThat(machine?.no).isEqualTo(no)
        assertThat(machine?.type).isEqualTo(MachineType.VENDING)
        assertThat(machine?.address).isEqualTo(address)
        assertThat(machine?.capacity).isEqualTo(100)
        assertThat(machine?.cupAmounts).isEqualTo(0)
        assertThat(machine?.state).isEqualTo(INITIAL)
    }

    @DisplayName("자판기 조회")
    @Test
    fun getTest() {
        //given
        val no = "0000000"
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(no, MachineType.VENDING, address, 100))
        clear()

        //when
        val machineDto = machineService.get(machine.id)

        //then
        assertThat(machineDto.no).isEqualTo(no)
        assertThat(machineDto.type).isEqualTo(MachineType.VENDING)
        assertThat(machineDto.address).isEqualTo(address)
        assertThat(machineDto.capacity).isEqualTo(100)
        assertThat(machineDto.cupAmounts).isEqualTo(0)
        assertThat(machineDto.state).isEqualTo(INITIAL)
    }
}