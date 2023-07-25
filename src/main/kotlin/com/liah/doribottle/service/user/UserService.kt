package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.repository.user.UserQueryRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.user.dto.UserDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class UserService(
    private val userRepository: UserRepository,
    private val userQueryRepository: UserQueryRepository
) {
    @Transactional(readOnly = true)
    fun get(id: UUID) = userQueryRepository.get(id).toDetailDto()

    @Transactional(readOnly = true)
    fun getAll(
        name: String? = null,
        phoneNumber: String? = null,
        birthDate: String? = null,
        gender: Gender? = null,
        active: Boolean? = null,
        blocked: Boolean? = null,
        groupId: UUID? = null,
        pageable: Pageable
    ): Page<UserDto> {
        return userQueryRepository.getAll(
            name = name,
            phoneNumber = phoneNumber,
            birthDate = birthDate,
            gender = gender,
            active = active,
            blocked = blocked,
            groupId = groupId,
            pageable = pageable
        ).map { it.toDto() }
    }

    fun update(
        id: UUID,
        name: String,
        birthDate: String,
        gender: Gender?
    ) {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.update(name, birthDate, gender)
    }
}