package com.liah.doribottle.service.account

import com.liah.doribottle.common.exception.BadRequestException
import com.liah.doribottle.common.exception.ErrorCode
import com.liah.doribottle.common.exception.NotFoundException
import com.liah.doribottle.common.exception.UnauthorizedException
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
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

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
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
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
        birthDate: Int,
        gender: Gender,
        agreedTermsOfService: Boolean,
        agreedTermsOfPrivacy: Boolean,
        agreedTermsOfMarketing: Boolean
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        if (user.phoneNumber != phoneNumber)
            throw BadRequestException(ErrorCode.INVALID_INPUT_VALUE)
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