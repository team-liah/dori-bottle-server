package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.repository.user.UserQueryRepository
import com.liah.doribottle.repository.user.UserRepository
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