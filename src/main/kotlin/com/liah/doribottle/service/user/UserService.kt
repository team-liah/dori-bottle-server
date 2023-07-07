package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.service.user.dto.UserDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
) {
    fun get(id: UUID): UserDto {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        return user.toDto()
    }
}