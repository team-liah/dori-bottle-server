package com.liah.doribottle.service.group

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.repository.group.GroupQueryRepository
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.group.dto.GroupDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupQueryRepository: GroupQueryRepository,
    private val userRepository: UserRepository,
    private val adminRepository: AdminRepository
) {
    fun register(
        name: String,
        type: GroupType,
        discountRate: Int
    ): UUID {
        val group = groupRepository.save(
            Group(
                name = name,
                type = type,
                discountRate = discountRate
            )
        )

        return group.id
    }

    @Transactional(readOnly = true)
    fun get(id: UUID): GroupDto {
        val group = groupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)

        return group.toDto()
    }

    @Transactional(readOnly = true)
    fun getAll(
        name: String? = null,
        type: GroupType? = null,
        pageable: Pageable
    ): Page<GroupDto> {
        return groupQueryRepository.getAll(
            name = name,
            type = type,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional(readOnly = true)
    fun findByCode(
        code: String
    ): GroupDto? {
        return groupRepository.findByCode(code)?.toDto()
    }

    @Transactional(readOnly = true)
    fun findByUserId(
        userId: UUID
    ): GroupDto? {
        return groupQueryRepository.findByUserId(userId)?.toDto()
    }

    fun update(
        id: UUID,
        name: String,
        type: GroupType,
        discountRate: Int
    ) {
        val group = groupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)

        group.update(name, type, discountRate)
    }

    fun delete(id: UUID) {
        val group = groupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)
        val groupUsers = userRepository.findAllByGroupId(id)
        val groupAdmins = adminRepository.findAllByGroupId(id)

        groupUsers.forEach { it.updateGroup(null) }
        groupAdmins.forEach { it.updateGroup(null) }

        groupRepository.delete(group)
    }
}