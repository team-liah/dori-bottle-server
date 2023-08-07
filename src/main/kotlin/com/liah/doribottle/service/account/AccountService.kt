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
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.event.point.PointSaveEvent
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.account.dto.AuthDto
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class AccountService(
    private val userRepository: UserRepository,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Transactional
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

    @Transactional
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
        userRepository.save(user)

        val accessToken = tokenProvider.generateAccessToken(
            id = user.id,
            loginId = user.loginId,
            name = user.name,
            role = user.role
        )
        val refreshToken = tokenProvider.generateRefreshToken(
            userId = user.id.toString()
        )

        return AuthDto(accessToken, refreshToken)
    }

    fun refreshAuth(
        refreshToken: String?
    ): AuthDto {
        val validRefreshToken = refreshToken?.let { tokenProvider.getRefreshToken(it) }
            ?: throw UnauthorizedException()
        val user = userRepository.findByIdOrNull(UUID.fromString(validRefreshToken.userId))
            ?: throw UnauthorizedException()

        verifyAccount(user)

        val accessToken = tokenProvider.generateAccessToken(
            id = user.id,
            loginId = user.loginId,
            name = user.name,
            role = user.role
        )
        val newRefreshToken = refresh(
            origin = validRefreshToken.refreshToken!!,
            userId = user.id
        )
        return AuthDto(accessToken, newRefreshToken)
    }

    private fun refresh(
        origin: String,
        userId: UUID
    ): String {
        tokenProvider.expireRefreshToken(origin)
        return tokenProvider.generateRefreshToken(userId.toString())
    }

    fun preAuth(doriUser: DoriUser) = tokenProvider.preAuthAccessToken(doriUser)

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

        val accessToken = tokenProvider.generateAccessToken(
            user.id,
            user.loginId,
            user.name,
            user.role
        )
        val refreshToken = tokenProvider.generateRefreshToken(
            user.id.toString()
        )
        return AuthDto(accessToken, refreshToken)
    }
}