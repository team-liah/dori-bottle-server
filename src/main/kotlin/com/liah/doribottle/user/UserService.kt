package com.liah.doribottle.user

import com.liah.doribottle.user.Role.ROLE_USER
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun join(
        phoneNumber: String,
        name: String
    ): UUID {
        validateDuplicateLoginId(phoneNumber)
        val user = userRepository.save(
            User(
                loginId = phoneNumber,
                name = name,
                phoneNumber = phoneNumber,
                role = ROLE_USER
            )
        )

        return user.id
    }

    private fun validateDuplicateLoginId(loginId: String) {
        if (userRepository.findByLoginId(loginId).isPresent) {
            throw IllegalArgumentException("이미 존재하는 회원입니다.")
        }
    }

    fun findUserByLoginId(loginId: String): UserDto? {
        return userRepository.findByLoginId(loginId).orElse(null)?.toDto()
    }
}