package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.BadRequestException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.RefreshToken
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.repository.user.RefreshTokenRepository
import com.liah.doribottle.repository.user.UserRepository
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
    fun register(
        loginId: String,
        name: String,
        birthDate: String,
        gender: Gender?,
        agreedTermsOfService: Boolean,
        agreedTermsOfPrivacy: Boolean,
        agreedTermsOfMarketing: Boolean
    ): UUID {
        val user = userRepository.findByLoginId(loginId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)
        if (user.role == Role.USER)
            throw BadRequestException(ErrorCode.USER_ALREADY_REGISTERED)

        user.update(name, birthDate, gender)
        user.agreeOnTerms(agreedTermsOfService, agreedTermsOfPrivacy, agreedTermsOfMarketing)
        user.changeRole(Role.USER)

        applicationEventPublisher.publishEvent(
            PointSaveEvent(
                user.id,
                PointSaveType.REWARD,
                PointEventType.SAVE_REGISTER_REWARD,
                SAVE_REGISTER_REWARD_AMOUNTS
            )
        )

        return user.id
    }

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

        verifyLoginPassword(user, loginPassword)
        verifyAccount(user)

        user.authSuccess()

        val accessToken = tokenProvider.createToken(
            user.id,
            user.loginId,
            user.name,
            user.role
        )
        val refreshToken = createRefreshToken(user)
        return AuthDto(accessToken, refreshToken)
    }

    fun refreshAuth(
        refreshToken: String?,
        millis: Long
    ): AuthDto {
        val validRefreshToken = refreshTokenRepository
            .findByTokenAndExpiredDateIsAfter(refreshToken, Instant.now())
            ?: throw UnauthorizedException()

        verifyAccount(validRefreshToken.user)

        validRefreshToken.refresh(millis)

        val accessToken = tokenProvider.createToken(
            validRefreshToken.user.id,
            validRefreshToken.user.loginId,
            validRefreshToken.user.name,
            validRefreshToken.user.role
        )
        val refreshedToken = validRefreshToken.token
        return AuthDto(accessToken, refreshedToken)
    }

    fun preAuth(doriUser: DoriUser) = tokenProvider.createPreAuthToken(doriUser)

    private fun verifyLoginPassword(
        user: User,
        loginPassword: String
    ) {
        if (user.loginExpirationDate == null
            || user.loginExpirationDate!! < Instant.now())
            throw BadCredentialsException("Login request is expired or does not exist.")
        if (!passwordEncoder.matches(loginPassword, user.loginPassword))
            throw BadCredentialsException("Invalid login password.")
    }

    private fun verifyAccount(user: User) {
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

    // TODO: Remove
    fun createDummyUser(
        loginId: String
    ) {
        val user = userRepository.findByLoginId(loginId)
        if (user == null) {
            userRepository.save(User(loginId, "강백호", loginId, Role.GUEST))
            register(loginId, "강백호", "19970224", Gender.MALE, true, true, true)
        }
    }

    // TODO: Remove
    fun dummyAuth(): AuthDto {
        val user = userRepository.findByLoginId("010-7777-7777")
            ?: throw UnauthorizedException()

        val accessToken = tokenProvider.createToken(
            user.id,
            user.loginId,
            user.name,
            user.role
        )
        val refreshToken = createRefreshToken(user)
        return AuthDto(accessToken, refreshToken)
    }
}