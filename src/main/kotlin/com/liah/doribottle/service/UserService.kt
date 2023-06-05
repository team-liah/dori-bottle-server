package com.liah.doribottle.service

import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.domain.user.UserRole
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    /**
     * Join user
     *
     * @param phoneNumber for loginId
     * @param name
     * @param role
     * @return result user's id
     * @throws IllegalArgumentException if duplicate phoneNumber(loginId)
     */
    @Transactional
    fun join(
        phoneNumber: String,
        name: String,
        role: UserRole
    ): UUID {
        verifyDuplicateLoginId(phoneNumber)
        val user = userRepository.save(
            User(
                loginId = phoneNumber,
                name = name,
                phoneNumber = phoneNumber,
                role = role
            )
        )

        return user.id
    }

    /**
     * Verify user's loginId is duplicated
     *
     * @param loginId
     * @throws IllegalArgumentException if duplicate loginId
     */
    private fun verifyDuplicateLoginId(
        loginId: String
    ) {
        if (userRepository.findByLoginId(loginId).isPresent) {
            throw IllegalArgumentException("이미 존재하는 회원입니다.")
        }
    }
}