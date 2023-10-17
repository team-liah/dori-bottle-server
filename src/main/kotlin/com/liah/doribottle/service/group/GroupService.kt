package com.liah.doribottle.service.group

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.repository.group.GroupQueryRepository
import com.liah.doribottle.repository.group.GroupRepository
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
    private val userRepository: UserRepository,
    private val groupQueryRepository: GroupQueryRepository
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

        groupUsers.forEach { it.updateGroup(null) }

        groupRepository.delete(group)
    }

    fun addUser(
        id: UUID,
        userId: UUID
    ) {
        val group = groupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.updateGroup(group)
    }

    fun removeUser(
        id: UUID,
        userId: UUID
    ) {
        val group = groupRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.GROUP_NOT_FOUND)
        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        if (user.group?.id != group.id) throw BusinessException(ErrorCode.GROUP_NOT_MEMBER)

        user.updateGroup(null)
    }
}