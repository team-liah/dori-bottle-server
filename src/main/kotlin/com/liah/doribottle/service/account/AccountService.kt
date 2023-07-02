package com.liah.doribottle.service.account

import com.liah.doribottle.common.exception.BadRequestException
import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.common.exception.UnauthorizedException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.service.account.dto.AuthDto
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
    private val refreshTokenRepository: RefreshTokenRepository,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder
) {
    fun updatePassword(
        loginId: String,
        loginPassword: String
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: userRepository.save(User(loginId, "일반 사용자", loginId, Role.GUEST))

        val encryptedPassword = passwordEncoder.encode(loginPassword)
        user.updatePassword(encryptedPassword)

        return user.id
    }

    fun auth(
        loginId: String,
        loginPassword: String
    ): AuthDto {
        val user = userRepository.findByLoginId(loginId)
            ?: throw UsernameNotFoundException("User $loginId was not found in the database) }")

        checkLoginPassword(user, loginPassword)
        checkAccount(user)

        user.authSuccess()

        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val refreshToken = createRefreshToken(user)
        return AuthDto(accessToken, refreshToken)
    }

    fun refreshAuth(
        loginId: String,
        refreshToken: String?,
        millis: Long
    ): AuthDto {
        val user = userRepository.findByLoginId(loginId)
            ?: throw NotFoundException("존재하지 않는 유저입니다.")
        val validRefreshToken = refreshTokenRepository
            .findByUserIdAndTokenAndExpiredDateIsAfter(user.id, refreshToken, Instant.now())
            ?: throw UnauthorizedException("유효한 토큰 정보를 확인할 수 없습니다.")

        checkAccount(user)

        validRefreshToken.refresh(millis)

        val accessToken = tokenProvider.createToken(user.id, user.loginId, user.role)
        val refreshedToken = validRefreshToken.token
        return AuthDto(accessToken, refreshedToken)
    }

    fun register(
        loginId: String,
        phoneNumber: String,
        name: String,
        birthDate: Int,
        gender: Gender
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: throw NotFoundException("존재하지 않는 유저입니다.")
        if (user.phoneNumber != phoneNumber)
            throw BadRequestException("잘못된 요청입니다.")

        user.update(name, birthDate, gender)
        user.changeRole(Role.USER)

        return user.id
    }

    private fun checkLoginPassword(
        user: User,
        loginPassword: String
    ) {
        if (user.loginExpirationDate == null
            || user.loginExpirationDate!! < Instant.now())
            throw BadCredentialsException("인증시간이 초과되었습니다.")
        if (!passwordEncoder.matches(loginPassword, user.loginPassword))
            throw BadCredentialsException("잘못된 인증번호입니다.")
    }

    private fun checkAccount(user: User) {
        if (!user.active)
            throw DisabledException("비활성화된 계정입니다.")
        if (user.blocked)
            throw LockedException("정지된 계정입니다.")
    }
    
    private fun createRefreshToken(
        user: User
    ): String {
        val refreshToken = refreshTokenRepository.save(RefreshToken(user))

        return refreshToken.token
    }
}