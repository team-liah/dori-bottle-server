package com.liah.doribottle.service.account

import com.liah.doribottle.common.exception.BadRequestException
import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class AccountService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun authRequest(
        loginId: String,
        loginPassword: String
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: userRepository.save(User(loginId, "일반 사용자", loginId, Role.GUEST))

        val encryptedPassword = passwordEncoder.encode(loginPassword)
        user.authRequest(encryptedPassword)

        return user.id
    }

    fun auth(
        loginId: String,
        loginPassword: String
    ): String {
        val user = userRepository.findByLoginId(loginId)
            ?: throw UsernameNotFoundException("User $loginId was not found in the database) }")

        if (user.loginExpirationDate == null
            || user.loginExpirationDate!! < Instant.now()) throw BadCredentialsException("인증시간이 초과되었습니다.")
        if (!passwordEncoder.matches(loginPassword, user.loginPassword)) throw BadCredentialsException("잘못된 인증번호입니다.")
        if (!user.active) throw DisabledException("비활성화된 계정입니다.")
        if (user.blocked) throw LockedException("정지된 계정입니다.")

        user.authSuccess()

        return tokenProvider.createToken(user.id, user.role)
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