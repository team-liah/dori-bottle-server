package com.liah.doribottle.service.account

import com.liah.doribottle.common.exception.BadRequestException
import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import com.liah.doribottle.utils.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AccountService(
    private val userRepository: UserRepository,

    @Value("\${jwt.secret}") val secretKey: String,
    @Value("\${jwt.expiredMs}") val expiredMs: Long
) {
    @Transactional
    fun auth(
        loginId: String,
        loginPassword: String
    ): String {
        val user = userRepository.findByLoginId(loginId)
            ?: throw UsernameNotFoundException("User $loginId was not found in the database) }")

        user.auth(loginPassword)

        return JwtUtil.createJwt(user.id, user.role, secretKey, expiredMs)
    }

    fun authRequest(
        loginId: String,
        loginPassword: String
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: userRepository.save(User(loginId, "일반 사용자", loginId, Role.GUEST))

        user.authRequest(loginPassword)

        return user.id
    }

    fun register(
        id: UUID,
        phoneNumber: String,
        name: String,
        birthDate: Int,
        gender: Gender
    ): UUID {
        val user = userRepository.findByIdOrNull(id)
            ?: throw NotFoundException("존재하지 않는 유저입니다.")
        if (user.phoneNumber != phoneNumber)
            throw BadRequestException("잘못된 요청입니다.")

        user.update(name, birthDate, gender)
        user.changeRole(Role.USER)

        return user.id
    }
}