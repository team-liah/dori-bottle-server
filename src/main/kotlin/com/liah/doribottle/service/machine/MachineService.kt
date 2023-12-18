package com.liah.doribottle.service.machine

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.config.security.acl.AclManager
import com.liah.doribottle.domain.machine.Machine
import com.liah.doribottle.domain.machine.MachineState
import com.liah.doribottle.domain.machine.MachineType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.machine.MachineQueryRepository
import com.liah.doribottle.repository.machine.MachineRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.common.AddressDto
import com.liah.doribottle.service.common.LocationDto
import com.liah.doribottle.service.machine.dto.MachineDetailDto
import com.liah.doribottle.service.machine.dto.MachineDto
import com.liah.doribottle.service.machine.dto.MachineSimpleDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.acls.domain.BasePermission
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import kotlin.reflect.jvm.jvmName

@Service
@Transactional
class MachineService(
    private val machineRepository: MachineRepository,
    private val machineQueryRepository: MachineQueryRepository,
    private val userRepository: UserRepository,
    private val groupRepository: GroupRepository,
    private val aclManager: AclManager
) {
    fun register(
        no: String,
        name: String,
        type: MachineType,
        address: AddressDto,
        location: LocationDto,
        capacity: Int,
        managerIds: Set<UUID> = emptySet(),
        groupCodes: Set<String> = emptySet()
    ): UUID {
        verifyDuplicatedNo(no)

        val machine = machineRepository.save(
            Machine(
                no = no,
                name = name,
                type = type,
                address = address.toEmbeddable(),
                location = location.toEmbeddable(),
                capacity = capacity
            )
        )

        aclManager.addPermissionForPrincipals(machine, BasePermission.READ, *managerIds.toTypedArray())
        aclManager.addPermissionForAuthorities(machine, BasePermission.READ, *groupCodes.toTypedArray())
        aclManager.addAllPermissionsForRoles(machine, Role.ADMIN, Role.SYSTEM)

        return machine.id
    }

    private fun verifyDuplicatedNo(no: String) {
        val machine = machineRepository.findByNo(no)
        if (machine != null)
            throw BusinessException(ErrorCode.MACHINE_ALREADY_REGISTERED)
    }

    @Transactional(readOnly = true)
    fun get(
        id: UUID
    ): MachineDto {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        return machine.toDto()
    }

    @Transactional(readOnly = true)
    fun getDetail(
        id: UUID
    ): MachineDetailDto {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        val managerIds = aclManager.getHasPermissionPrincipals(machine, BasePermission.READ)
        val managementGroupCodes = aclManager.getHasPermissionAuthorities(machine, BasePermission.READ)
        val managers = userRepository.findAllById(managerIds)
        val managementGroups = groupRepository.findAllByCodeIn(managementGroupCodes)

        return machine.toDetailDto(managers, managementGroups)
    }

    @Transactional(readOnly = true)
    fun getAll(
        ids: List<UUID>? = null,
        no: String? = null,
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<MachineDto> {
        return machineQueryRepository.getAll(
            ids = ids,
            no = no,
            name = name,
            type = type,
            state = state,
            addressKeyword = addressKeyword,
            deleted = deleted,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun getAllSimple(
        ids: List<UUID>? = null,
        no: String? = null,
        name: String? = null,
        type: MachineType? = null,
        state: MachineState? = null,
        addressKeyword: String? = null,
        deleted: Boolean? = null
    ): List<MachineSimpleDto> {
        return machineQueryRepository.getAll(
            ids = ids,
            no = no,
            name = name,
            type = type,
            state = state,
            addressKeyword = addressKeyword,
            deleted = deleted
        )
    }

    @Transactional(readOnly = true)
    fun getAccessibleIds(
        principal: UUID? = null,
        authorities: List<String> = emptyList()
    ): List<UUID> {
        return aclManager.getAuthorizedObjectIds(
            type = Machine::class.jvmName,
            permission = BasePermission.READ,
            principal = principal,
            *authorities.toTypedArray()
        )
    }

    fun update(
        id: UUID,
        name: String,
        address: AddressDto,
        location: LocationDto,
        capacity: Int,
        cupAmounts: Int,
        state: MachineState,
        managerIds: Set<UUID> = emptySet(),
        managementGroupCodes: Set<String> = emptySet()
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.update(
            name = name,
            address = address.toEmbeddable(),
            location = location.toEmbeddable(),
            capacity = capacity,
            cupAmounts = cupAmounts,
            state = state
        )

        updateManagers(machine, managerIds)
        updateManagementGroups(machine, managementGroupCodes)
    }

    private fun updateManagers(machine: Machine, managerIds: Set<UUID>) {
        val originManagerIds = aclManager.getHasPermissionPrincipals(machine, BasePermission.READ).toSet()
        val addManagerIds = managerIds - originManagerIds
        val removeManagerIds = originManagerIds - managerIds
        aclManager.addPermissionForPrincipals(machine, BasePermission.READ, *addManagerIds.toTypedArray())
        aclManager.removePermissionForPrincipals(machine, BasePermission.READ, *removeManagerIds.toTypedArray())
    }

    private fun updateManagementGroups(machine: Machine, managementGroupCodes: Set<String>) {
        val originManagementGroupCodes = aclManager
            .getHasPermissionAuthorities(machine, BasePermission.READ)
            .filter { it.startsWith("GROUP_") }
            .toSet()
        val addManagementGroupCodes = managementGroupCodes - originManagementGroupCodes
        val removeManagementGroupCodes = originManagementGroupCodes - managementGroupCodes
        aclManager.addPermissionForAuthorities(machine, BasePermission.READ, *addManagementGroupCodes.toTypedArray())
        aclManager.removePermissionForAuthorities(machine, BasePermission.READ, *removeManagementGroupCodes.toTypedArray())
    }

    fun delete(
        id: UUID
    ) {
        val machine = machineRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.MACHINE_NOT_FOUND)

        machine.delete()

        aclManager.removeObjectIdentity(machine)
    }

    // TODO: Remove
    fun createDummyMachine(
        no: String,
        no2: String
    ) {
        val vending = machineRepository.findByNo(no)
        if (vending == null) {
            register(no, "삼성역점", MachineType.VENDING, AddressDto("12345", "서울시", "삼성동"), LocationDto(37.508855, 127.059479), 100)
        }

        val collection = machineRepository.findByNo(no2)
        if (collection == null) {
            register(no2, "코엑스점", MachineType.COLLECTION, AddressDto("12345", "서울시", "삼성동"), LocationDto(37.508276, 127.055314), 100)
        }
    }
}