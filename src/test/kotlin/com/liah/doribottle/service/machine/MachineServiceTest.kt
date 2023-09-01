package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.common.AddressDto
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class MachineServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var machineRepository: MachineRepository
    @Autowired
    private lateinit var machineService: MachineService

    @DisplayName("자판기 등록")
    @Test
    fun register() {
        //given
        val address = AddressDto("12345", "삼성로", null)

        //when
        val id = machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address, 100)
        clear()

        //then
        val machine = machineRepository.findByIdOrNull(id)
        assertThat(machine?.no).isEqualTo(MACHINE_NO)
        assertThat(machine?.name).isEqualTo(MACHINE_NAME)
        assertThat(machine?.type).isEqualTo(VENDING)
        assertThat(machine?.address?.toDto()).isEqualTo(address)
        assertThat(machine?.capacity).isEqualTo(100)
        assertThat(machine?.cupAmounts).isEqualTo(0)
        assertThat(machine?.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 등록 예외")
    @Test
    fun registerException() {
        //given
        val address = Address("12345", "삼성로", null)
        machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, 100))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address.toDto(), 100)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.MACHINE_ALREADY_REGISTERED)
    }

    @DisplayName("자판기 조회")
    @Test
    fun get() {
        //given
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, 100))
        clear()

        //when
        val machineDto = machineService.get(machine.id)

        //then
        assertThat(machineDto.no).isEqualTo(MACHINE_NO)
        assertThat(machineDto.name).isEqualTo(MACHINE_NAME)
        assertThat(machineDto.type).isEqualTo(VENDING)
        assertThat(machineDto.address).isEqualTo(address.toDto())
        assertThat(machineDto.capacity).isEqualTo(100)
        assertThat(machineDto.cupAmounts).isEqualTo(0)
        assertThat(machineDto.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 목록 조회")
    @Test
    fun getAll() {
        //given
        insertMachines()
        clear()

        //when
        val result = machineService.getAll(null, null, null, null, null, Pageable.unpaged())

        //then
        assertThat(result)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003", "0000004", "0000005", "0000006",)
    }

    @DisplayName("자판기 목록 조회 - 필터")
    @Test
    fun getAllUseFilter() {
        //given
        insertMachines()
        clear()

        //when
        val result1 = machineService.getAll(null, null, null, null, "도산대로", Pageable.unpaged())
        val result2 = machineService.getAll(null, null, COLLECTION, null, null, Pageable.unpaged())
        val result3 = machineService.getAll(null, null, null, NORMAL, "삼성", Pageable.unpaged())

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
        insertMachines()
        clear()

        //when
        val result1 = machineService.getAll(null, null, null, null, "도산대로", Pageable.ofSize(1))
        val result2 = machineService.getAll(null, null, null, null, null, Pageable.ofSize(3))

        //then
        assertThat(result1)
            .extracting("no")
            .containsExactly("0000005")
        assertThat(result2)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003")
    }

    fun insertMachines() {
        machineRepository.save(Machine("0000001", MACHINE_NAME, VENDING, Address("00001", "삼성로", null), 100))
        machineRepository.save(Machine("0000002", MACHINE_NAME, VENDING, Address("00002", "삼성로", null), 100))
        machineRepository.save(Machine("0000003", MACHINE_NAME, VENDING, Address("00003", "삼성로", null), 100))
        machineRepository.save(Machine("0000004", MACHINE_NAME, VENDING, Address("00004", "마장로", null), 100))
        machineRepository.save(Machine("0000005", MACHINE_NAME, COLLECTION, Address("00005", "도산대로", null), 100))
        machineRepository.save(Machine("0000006", MACHINE_NAME, VENDING, Address("00006", "도산대로", null), 100))
    }

    @DisplayName("자판기 정보 수정")
    @Test
    fun update() {
        //given
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, 100))
        clear()

        //when
        val newAddress = AddressDto("00000", "마장로", null)
        machineService.update(machine.id, "new name", newAddress, 200, 10)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.no).isEqualTo(MACHINE_NO)
        assertThat(findMachine?.name).isEqualTo("new name")
        assertThat(findMachine?.type).isEqualTo(VENDING)
        assertThat(findMachine?.address?.zipCode).isEqualTo("00000")
        assertThat(findMachine?.address?.address1).isEqualTo("마장로")
        assertThat(findMachine?.address?.address2).isNull()
        assertThat(findMachine?.capacity).isEqualTo(200)
        assertThat(findMachine?.cupAmounts).isEqualTo(10)
        assertThat(findMachine?.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 컵 개수 수정")
    @Test
    fun updateCupAmounts() {
        //given
        val address = Address("12345", "삼성로", null)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, 100))
        clear()

        //when
        machineService.updateCupAmounts(machine.id, 100)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.cupAmounts).isEqualTo(100)
    }
}