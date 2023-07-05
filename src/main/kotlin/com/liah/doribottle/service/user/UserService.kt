package com.liah.doribottle.service.user

import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.service.user.dto.UserDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.webjars.NotFoundException
import java.util.*

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
) {
    fun get(id: UUID): UserDto {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("존재하지 않는 유저입니다.")

        return user.toDto()
    }
}