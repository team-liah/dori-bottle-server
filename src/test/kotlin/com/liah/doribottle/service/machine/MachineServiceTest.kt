package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.config.security.acl.AclManager
import com.liah.doribottle.domain.common.Address
import com.liah.doribottle.domain.common.Location
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState.MALFUNCTION
import com.liah.doribottle.domain.machine.MachineState.NORMAL
import com.liah.doribottle.domain.machine.MachineType.COLLECTION
import com.liah.doribottle.domain.machine.MachineType.VENDING
import com.liah.doribottle.domain.user.Role
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
import org.springframework.security.acls.domain.BasePermission
import java.util.*

class MachineServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var machineRepository: MachineRepository
    @Autowired
    private lateinit var machineService: MachineService
    @Autowired
    private lateinit var aclManager: AclManager

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 등록")
    @Test
    fun register() {
        //given
        val address = AddressDto("12345", "삼성로", null)
        val location = LocationDto(37.508855, 127.059479)
        val managerIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        val managementGroupCodes = setOf("GROUP_UNIVERSITY_1", "GROUP_UNIVERSITY_2")

        //when
        val id = machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100, managerIds.toSet(), managementGroupCodes)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(id)!!
        val findPrincipals = aclManager.getHasPermissionPrincipals(findMachine, BasePermission.READ)
        val findAuthorities = aclManager.getHasPermissionAuthorities(findMachine, BasePermission.READ)

        assertThat(findMachine.no).isEqualTo(MACHINE_NO)
        assertThat(findMachine.name).isEqualTo(MACHINE_NAME)
        assertThat(findMachine.type).isEqualTo(VENDING)
        assertThat(findMachine.address.toDto()).isEqualTo(address)
        assertThat(findMachine.location.toDto()).isEqualTo(location)
        assertThat(findMachine.capacity).isEqualTo(100)
        assertThat(findMachine.cupAmounts).isEqualTo(0)
        assertThat(findMachine.state).isEqualTo(NORMAL)

        assertThat(findPrincipals)
            .containsExactlyInAnyOrder(managerIds[0], managerIds[1])

        assertThat(findAuthorities)
            .containsExactlyInAnyOrder(Role.ADMIN.key, Role.SYSTEM.key, "GROUP_UNIVERSITY_1", "GROUP_UNIVERSITY_2")
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 등록 TC2")
    @Test
    fun registerTc2() {
        //given
        val address = AddressDto("12345", "삼성로", null)
        val location = LocationDto(37.508855, 127.059479)

        //when
        val id = machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(id)!!
        val findPrincipals = aclManager.getHasPermissionPrincipals(findMachine, BasePermission.READ)
        val findAuthorities = aclManager.getHasPermissionAuthorities(findMachine, BasePermission.READ)

        assertThat(findMachine.no).isEqualTo(MACHINE_NO)
        assertThat(findMachine.name).isEqualTo(MACHINE_NAME)
        assertThat(findMachine.type).isEqualTo(VENDING)
        assertThat(findMachine.address.toDto()).isEqualTo(address)
        assertThat(findMachine.location.toDto()).isEqualTo(location)
        assertThat(findMachine.capacity).isEqualTo(100)
        assertThat(findMachine.cupAmounts).isEqualTo(0)
        assertThat(findMachine.state).isEqualTo(NORMAL)

        assertThat(findPrincipals).isEmpty()

        assertThat(findAuthorities)
            .containsExactlyInAnyOrder(Role.ADMIN.key, Role.SYSTEM.key)
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 등록 예외")
    @Test
    fun registerException() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
        clear()

        //when, then
        val exception = assertThrows<BusinessException> {
            machineService.register(MACHINE_NO, MACHINE_NAME, VENDING, address.toDto(), location.toDto(), 100)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.MACHINE_ALREADY_REGISTERED)
    }

    @DisplayName("자판기 조회")
    @Test
    fun get() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
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
        val result = machineService.getAll(null, null, null, null, null, null, false, Pageable.unpaged())

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
        val result1 = machineService.getAll(null, null, null, null, null, "도산대로", false, Pageable.unpaged())
        val result2 = machineService.getAll(null, null, null, COLLECTION, null, null, false, Pageable.unpaged())
        val result3 = machineService.getAll(null, null, null, null, NORMAL, "삼성", false, Pageable.unpaged())

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
        val result1 = machineService.getAll(null, null, null, null, null, "도산대로", null, Pageable.ofSize(1))
        val result2 = machineService.getAll(null, null, null, null, null, null, null, Pageable.ofSize(3))

        //then
        assertThat(result1)
            .extracting("no")
            .containsExactly("0000005")
        assertThat(result2)
            .extracting("no")
            .containsExactly("0000001", "0000002", "0000003")
    }

    @DisplayName("Simple 자판기 목록 조회 - 논페이징")
    @Test
    fun getAllSimple() {
        //given
        insertMachines()
        clear()

        val result = machineService.getAllSimple(state = NORMAL, deleted = false)

        assertThat(result)
            .extracting("type")
            .containsExactly(VENDING, VENDING, VENDING, VENDING, COLLECTION)
        assertThat(result)
            .extracting("location.latitude")
            .containsExactly(37.508855, 37.508955, 37.508355, 37.508455, 37.518855)
        assertThat(result)
            .extracting("location.longitude")
            .containsExactly(127.059479, 127.052479, 127.051479, 127.053479, 127.029479)
    }

    fun insertMachines() {
        val machine1 = machineRepository.save(Machine("0000001", MACHINE_NAME, VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100))
        val machine2 = machineRepository.save(Machine("0000002", MACHINE_NAME, VENDING, Address("00002", "삼성로", null), Location(37.508955, 127.052479), 100))
        val machine3 = machineRepository.save(Machine("0000003", MACHINE_NAME, VENDING, Address("00003", "삼성로", null), Location(37.508355, 127.051479), 100))
        val machine4 = machineRepository.save(Machine("0000004", MACHINE_NAME, VENDING, Address("00004", "마장로", null), Location(37.508455, 127.053479), 100))
        val machine5 = machineRepository.save(Machine("0000005", MACHINE_NAME, COLLECTION, Address("00005", "도산대로", null), Location(37.518855, 127.029479), 100))
        val machine6 = Machine("0000006", MACHINE_NAME, VENDING, Address("00006", "도산대로", null), Location(37.503855, 127.059179), 100)
        val machine7 = Machine("0000007", MACHINE_NAME, VENDING, Address("00001", "삼성로", null), Location(37.508855, 127.059479), 100)

        machine6.update(MACHINE_NAME, machine6.address, machine6.location, machine6.capacity, machine6.cupAmounts, MALFUNCTION)
        machineRepository.save(machine6)

        machine7.delete()
        machineRepository.save(machine7)
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 정보 수정")
    @Test
    fun update() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
        clear()

        //when
        val newAddress = AddressDto("00000", "마장로", null)
        val newLocation = LocationDto(37.508855, 127.029479)
        machineService.update(machine.id, "new name", newAddress, newLocation, 200, 10, MALFUNCTION)
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

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 정보 수정 TC2")
    @Test
    fun updateTc2() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
        val managerIds = listOf(UUID.randomUUID(), UUID.randomUUID())
        val managementGroupCodes = setOf("GROUP_UNIVERSITY_1", "GROUP_UNIVERSITY_2")
        aclManager.addPermissionForPrincipals(machine, BasePermission.READ, *managerIds.toTypedArray())
        aclManager.addPermissionForAuthorities(machine, BasePermission.READ, *managementGroupCodes.toTypedArray())
        clear()

        //when
        val newAddress = AddressDto("00000", "마장로", null)
        val newLocation = LocationDto(37.508855, 127.029479)
        val newManagerIds = setOf(UUID.randomUUID())
        val newManagementGroupCodes = setOf("GROUP_UNIVERSITY_2", "GROUP_UNIVERSITY_3")
        machineService.update(machine.id, "new name", newAddress, newLocation, 200, 10, MALFUNCTION, newManagerIds, newManagementGroupCodes)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)!!
        val findPrincipals = aclManager.getHasPermissionPrincipals(findMachine, BasePermission.READ)
        val findAuthorities = aclManager.getHasPermissionAuthorities(findMachine, BasePermission.READ)

        assertThat(findMachine.no).isEqualTo(MACHINE_NO)
        assertThat(findMachine.name).isEqualTo("new name")
        assertThat(findMachine.type).isEqualTo(VENDING)
        assertThat(findMachine.address.zipCode).isEqualTo("00000")
        assertThat(findMachine.address.address1).isEqualTo("마장로")
        assertThat(findMachine.address.address2).isNull()
        assertThat(findMachine.location.latitude).isEqualTo(37.508855)
        assertThat(findMachine.location.longitude).isEqualTo(127.029479)
        assertThat(findMachine.capacity).isEqualTo(200)
        assertThat(findMachine.cupAmounts).isEqualTo(10)
        assertThat(findMachine.state).isEqualTo(MALFUNCTION)

        assertThat(findPrincipals)
            .containsExactlyInAnyOrder(*newManagerIds.toTypedArray())

        assertThat(findAuthorities)
            .containsExactlyInAnyOrder("GROUP_UNIVERSITY_2", "GROUP_UNIVERSITY_3")
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 삭제")
    @Test
    fun delete() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
        clear()

        //when
        machineService.delete(machine.id)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)
        assertThat(findMachine?.no).startsWith("Deleted")
        assertThat(findMachine?.deleted).isTrue()
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("자판기 삭제 TC2")
    @Test
    fun deleteTc2() {
        //given
        val address = Address("12345", "삼성로", null)
        val location = Location(37.508855, 127.059479)
        val managementGroupCodes = setOf("GROUP_UNIVERSITY_1", "GROUP_UNIVERSITY_2")
        val machine = machineRepository.save(Machine(MACHINE_NO, MACHINE_NAME, VENDING, address, location, 100))
        aclManager.addPermissionForAuthorities(machine, BasePermission.READ, *managementGroupCodes.toTypedArray())
        clear()

        //when
        machineService.delete(machine.id)
        clear()

        //then
        val findMachine = machineRepository.findByIdOrNull(machine.id)!!
        val findAuthorities = aclManager.getHasPermissionAuthorities(findMachine, BasePermission.READ)

        assertThat(findMachine.no).startsWith("Deleted")
        assertThat(findMachine.deleted).isTrue()

        assertThat(findAuthorities).isEmpty()
    }
}