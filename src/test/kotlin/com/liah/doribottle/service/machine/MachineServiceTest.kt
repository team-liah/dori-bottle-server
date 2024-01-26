package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState.MALFUNCTION
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
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
        val location = LocationDto(37.508855, 127.059479)

        //when
        val id = machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, null)
        clear()

        //then
        val machine = machineRepository.findByIdOrNull(id)
        assertThat(machine?.no).isEqualTo(MACHINE_NO)
        assertThat(machine?.name).isEqualTo(MACHINE_NAME)
        assertThat(machine?.type).isEqualTo(VENDING)
        assertThat(machine?.address?.toDto()).isEqualTo(address)
        assertThat(machine?.location?.toDto()).isEqualTo(location)
        assertThat(machine?.capacity).isEqualTo(100)
        assertThat(machine?.cupAmounts).isEqualTo(0)
        assertThat(machine?.state).isEqualTo(NORMAL)
    }

    @DisplayName("자판기 등록 예외")
    @Test
    fun registerException() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, null))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address.toDto(), location.toDto(), 100, null)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.MACHINE_ALREADY_REGISTERED)
    }

    @DisplayName("자판기 조회")
    @Test
    fun get() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, null))
        clear()

        //when
        val machineDto = machineService.get(machine.id)

        //then
        assertThat(machineDto.no).isEqualTo(MACHINE_NO)
        assertThat(machineDto.name).isEqualTo(MACHINE_NAME)
        assertThat(machineDto.type).isEqualTo(VENDING)
        assertThat(machineDto.address).isEqualTo(address.toDto())
        assertThat(machineDto.location).isEqualTo(location.toDto())
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
        val result = machineService.getAll(null, null, null, null, null, null, Pageable.unpaged())

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
        val result1 = machineService.getAll(null, null, null, null, "도산대로", null, Pageable.unpaged())
        val result2 = machineService.getAll(null, null, COLLECTION, null, null, null, Pageable.unpaged())
        val result3 = machineService.getAll(null, null, null, NORMAL, "삼성", null, Pageable.unpaged())

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
        val result1 = machineService.getAll(null, null, null, null, "도산대로", null, Pageable.ofSize(1))
        val result2 = machineService.getAll(null, null, null, null, null, null, Pageable.ofSize(3))

        //then
        assertThat(result1)
            .extracting("no")
            .containsExactly("0000005")
        assertThat(result2)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003")
    }

    @DisplayName("자판기 목록 조회 - 페이징")
    @Test
    fun getAllNotUsePaging() {
        //given
        insertMachines()
        clear()

        val result = machineService.getAll()

        assertThat(result)
            .extracting("type")
            .containsExactly(VENDING, VENDING, VENDING, VENDING, COLLECTION, VENDING)
        assertThat(result)
            .extracting("location.latitude")
            .containsExactly(37.508855, 37.508955, 37.508355, 37.508455, 37.518855, 37.503855)
        assertThat(result)
            .extracting("location.longitude")
            .containsExactly(127.059479, 127.052479, 127.051479, 127.053479, 127.029479, 127.059179)
    }

    fun insertMachines() {
        machineRepository.save(Machine("0000001", MACHINE_NAME, VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100, null))
        machineRepository.save(Machine("0000002", MACHINE_NAME, VENDING, Address("00002", "삼성로", null), Location(37.508955, 127.052479), 100, null))
        machineRepository.save(Machine("0000003", MACHINE_NAME, VENDING, Address("00003", "삼성로", null), Location(37.508355, 127.051479), 100, null))
        machineRepository.save(Machine("0000004", MACHINE_NAME, VENDING, Address("00004", "마장로", null), Location(37.508455, 127.053479), 100, null))
        machineRepository.save(Machine("0000005", MACHINE_NAME, COLLECTION, Address("00005", "도산대로", null), Location(37.518855, 127.029479), 100, null))
        machineRepository.save(Machine("0000006", MACHINE_NAME, VENDING, Address("00006", "도산대로", null), Location(37.503855, 127.059179), 100, null))
    }

    @DisplayName("자판기 정보 수정")
    @Test
    fun update() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, null))
        clear()

        //when
        val newAddress = AddressDto("00000", "마장로", null)
        val newLocation = LocationDto(37.508855, 127.029479)
        machineService.update(machine.id, "new name", newAddress, newLocation, 200, 10, MALFUNCTION, null)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.no).isEqualTo(MACHINE_NO)
        assertThat(findMachine?.name).isEqualTo("new name")
        assertThat(findMachine?.type).isEqualTo(VENDING)
        assertThat(findMachine?.address?.zipCode).isEqualTo("00000")
        assertThat(findMachine?.address?.address1).isEqualTo("마장로")
        assertThat(findMachine?.address?.address2).isNull()
        assertThat(findMachine?.location?.latitude).isEqualTo(37.508855)
        assertThat(findMachine?.location?.longitude).isEqualTo(127.029479)
        assertThat(findMachine?.capacity).isEqualTo(200)
        assertThat(findMachine?.cupAmounts).isEqualTo(10)
        assertThat(findMachine?.state).isEqualTo(MALFUNCTION)
    }

    @DisplayName("자판기 삭제")
    @Test
    fun delete() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, null))
        clear()

        //when
        machineService.delete(machine.id)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.no).startsWith("Deleted")
        assertThat(findMachine?.deleted).isTrue()
    }
}