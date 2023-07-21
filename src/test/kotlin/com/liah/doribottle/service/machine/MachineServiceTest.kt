package com.liah.doribottle.service.machine

import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.repository.machine.MachineRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Pageable
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

    companion object {
        const val NO = "0000000"
        const val NAME = "XX대학교 정문"
    }

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("자판기 등록")
    @Test
    fun register() {
        //given
        val address = Address("12345", "삼성로", null)

        //when
        val id = machineService.register(NO, NAME, VENDING, address, 100)
        clear()

        //then
        val machine = machineRepository.findByIdOrNull(id)
        assertThat(machine?.no).isEqualTo(NO)
        assertThat(machine?.name).isEqualTo(NAME)
        assertThat(machine?.type).isEqualTo(VENDING)
        assertThat(machine?.address).isEqualTo(address)
        assertThat(machine?.capacity).isEqualTo(100)
        assertThat(machine?.cupAmounts).isEqualTo(0)
        assertThat(machine?.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 조회")
    @Test
    fun get() {
        //given
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(NO, NAME, VENDING, address, 100))
        clear()

        //when
        val machineDto = machineService.get(machine.id)

        //then
        assertThat(machineDto.no).isEqualTo(NO)
        assertThat(machineDto.name).isEqualTo(NAME)
        assertThat(machineDto.type).isEqualTo(VENDING)
        assertThat(machineDto.address).isEqualTo(address)
        assertThat(machineDto.capacity).isEqualTo(100)
        assertThat(machineDto.cupAmounts).isEqualTo(0)
        assertThat(machineDto.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 목록 조회")
    @Test
    fun getAll() {
        //given
        machineRepository.save(Machine("0000001", NAME, VENDING, Address("00001", "삼성로", null), 100))
        machineRepository.save(Machine("0000002", NAME, VENDING, Address("00002", "삼성로", null), 100))
        machineRepository.save(Machine("0000003", NAME, VENDING, Address("00003", "삼성로", null), 100))
        machineRepository.save(Machine("0000004", NAME, VENDING, Address("00004", "마장로", null), 100))
        machineRepository.save(Machine("0000005", NAME, COLLECTION, Address("00005", "도산대로", null), 100))
        machineRepository.save(Machine("0000006", NAME, VENDING, Address("00006", "도산대로", null), 100))
        clear()

        //when
        val result = machineService.getAll(null, null, null, null, Pageable.unpaged())

        //then
        assertThat(result)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003", "0000004", "0000005", "0000006",)
    }

    @DisplayName("자판기 목록 조회 - 필터")
    @Test
    fun getAllUseFilter() {
        //given
        machineRepository.save(Machine("0000001", NAME, VENDING, Address("00001", "삼성로", null), 100))
        machineRepository.save(Machine("0000002", NAME, VENDING, Address("00002", "삼성로", null), 100))
        machineRepository.save(Machine("0000003", NAME, VENDING, Address("00003", "삼성로", null), 100))
        machineRepository.save(Machine("0000004", NAME, VENDING, Address("00004", "마장로", null), 100))
        machineRepository.save(Machine("0000005", NAME, COLLECTION, Address("00005", "도산대로", null), 100))
        machineRepository.save(Machine("0000006", NAME, VENDING, Address("00006", "도산대로", null), 100))
        clear()

        //when
        val result1 = machineService.getAll(null, null, null, "도산대로", Pageable.unpaged())
        val result2 = machineService.getAll(null, COLLECTION, null, null, Pageable.unpaged())
        val result3 = machineService.getAll(null, null, NORMAL, "삼성", Pageable.unpaged())

        //then
        assertThat(result1)
            .extracting("no")
            .containsExactly("0000005", "0000006")
        assertThat(result2)
            .extracting("no")
            .containsExactly("0000005")
        assertThat(result3)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003")
    }

    @DisplayName("자판기 목록 조회 - 페이징")
    @Test
    fun getAllUsePaging() {
        //given
        machineRepository.save(Machine("0000001", NAME, VENDING, Address("00001", "삼성로", null), 100))
        machineRepository.save(Machine("0000002", NAME, VENDING, Address("00002", "삼성로", null), 100))
        machineRepository.save(Machine("0000003", NAME, VENDING, Address("00003", "삼성로", null), 100))
        machineRepository.save(Machine("0000004", NAME, VENDING, Address("00004", "마장로", null), 100))
        machineRepository.save(Machine("0000005", NAME, COLLECTION, Address("00005", "도산대로", null), 100))
        machineRepository.save(Machine("0000006", NAME, VENDING, Address("00006", "도산대로", null), 100))
        clear()

        //when
        val result1 = machineService.getAll(null, null, null, "도산대로", Pageable.ofSize(1))
        val result2 = machineService.getAll(null, null, null, null, Pageable.ofSize(3))

        //then
        assertThat(result1)
            .extracting("no")
            .containsExactly("0000005")
        assertThat(result2)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003")
    }

    @DisplayName("자판기 정보 수정")
    @Test
    fun update() {
        //given
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(NO, NAME, VENDING, address, 100))
        clear()

        //when
        val newAddress = Address("00000", "마장로", null)
        machineService.update(machine.id, "new name", newAddress, 200, 10)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.no).isEqualTo(NO)
        assertThat(findMachine?.name).isEqualTo("new name")
        assertThat(findMachine?.type).isEqualTo(VENDING)
        assertThat(findMachine?.address?.zipCode).isEqualTo("00000")
        assertThat(findMachine?.address?.address1).isEqualTo("마장로")
        assertThat(findMachine?.address?.address2).isNull()
        assertThat(findMachine?.capacity).isEqualTo(200)
        assertThat(findMachine?.cupAmounts).isEqualTo(10)
        assertThat(findMachine?.state).isEqualTo(NORMAL)
    }
}