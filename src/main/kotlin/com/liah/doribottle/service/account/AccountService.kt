package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.BadRequestException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointHistoryType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.service.account.dto.AuthDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
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
    private val passwordEncoder: PasswordEncoder,
    private val applicationEventPublisher: ApplicationEventPublisher
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
            ?: throw UnauthorizedException()

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
            ?: throw UnauthorizedException()
        val validRefreshToken = refreshTokenRepository
            .findByUserIdAndTokenAndExpiredDateIsAfter(user.id, refreshToken, Instant.now())
            ?: throw UnauthorizedException()

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
        birthDate: String,
        gender: Gender,
        agreedTermsOfService: Boolean,
        agreedTermsOfPrivacy: Boolean,
        agreedTermsOfMarketing: Boolean
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        if (user.phoneNumber != phoneNumber)
            throw BadRequestException(ErrorCode.USER_INVALID_PHONE_NUMBER)
        if (user.role == Role.USER)
            throw BadRequestException(ErrorCode.USER_ALREADY_REGISTERED)

        user.update(name, birthDate, gender)
        user.agreeOnTerms(agreedTermsOfService, agreedTermsOfPrivacy, agreedTermsOfMarketing)
        user.changeRole(Role.USER)

        applicationEventPublisher.publishEvent(
            PointSaveEvent(
                user.id,
                PointSaveType.REWARD,
                PointHistoryType.SAVE_REGISTER_REWARD,
                SAVE_REGISTER_REWARD_AMOUNTS
            )
        )

        return user.id
    }

    private fun checkLoginPassword(
        user: User,
        loginPassword: String
    ) {
        if (user.loginExpirationDate == null
            || user.loginExpirationDate!! < Instant.now())
            throw BadCredentialsException("Login request is expired or does not exist.")
        if (!passwordEncoder.matches(loginPassword, user.loginPassword))
            throw BadCredentialsException("Invalid login password.")
    }

    private fun checkAccount(user: User) {
        if (!user.active)
            throw DisabledException("Account is disabled.")
        if (user.blocked)
            throw LockedException("Account is locked.")
    }
    
    private fun createRefreshToken(
        user: User
    ): String {
        val refreshToken = refreshTokenRepository.save(RefreshToken(user))

        return refreshToken.token
    }
}