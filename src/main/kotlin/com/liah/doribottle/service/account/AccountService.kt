package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.*
import com.liah.doribottle.config.security.DoriUser
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.constant.SAVE_REGISTER_REWARD_AMOUNTS
import com.liah.doribottle.domain.point.PointEventType
import com.liah.doribottle.domain.point.PointSaveType
import com.liah.doribottle.domain.user.Gender
import com.liah.doribottle.domain.user.LoginIdChange
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.payment.PaymentMethodRepository
import com.liah.doribottle.repository.user.LoginIdChangeRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.account.dto.AuthDto
import com.liah.doribottle.service.sqs.AwsSqsSender
import com.liah.doribottle.service.sqs.dto.PointSaveMessage
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class AccountService(
    private val userRepository: UserRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val loginIdChangeRepository: LoginIdChangeRepository,
    private val awsSqsSender: AwsSqsSender,
    private val tokenProvider: TokenProvider,
    private val passwordEncoder: PasswordEncoder
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
        user.register()

        awsSqsSender.send(
            PointSaveMessage(
                userId = user.id,
                saveType = PointSaveType.REWARD,
                eventType = PointEventType.SAVE_REGISTER_REWARD,
                saveAmounts = SAVE_REGISTER_REWARD_AMOUNTS
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

    @Transactional(readOnly = true)
    fun preAuth(doriUser: DoriUser): String {
        val user = userRepository.findByIdOrNull(doriUser.id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        verifyCanRent(user)

        return tokenProvider.preAuthAccessToken(doriUser)
    }

    private fun verifyCanRent(user: User) {
        if (user.blocked)
            throw ForbiddenException(ErrorCode.BLOCKED_USER_ACCESS_DENIED)
        paymentMethodRepository.findFirstByUserIdAndDefault(user.id, true)
            ?: throw NotFoundException(ErrorCode.PAYMENT_METHOD_NOT_FOUND)
    }

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
    }

    @Transactional
    fun deactivate(
        id: UUID
    ) {
       val user = userRepository.findByIdOrNull(id)
           ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

       user.deactivate()
    }

    fun createLoginIdChange(
        userId: UUID,
        toLoginId: String,
        authCode: String
    ) {
        verifyDuplicatedLoginId(toLoginId)

        loginIdChangeRepository.save(
            LoginIdChange(
                userId = userId.toString(),
                toLoginId = toLoginId,
                authCode = authCode
            )
        )
    }

    fun changeLoginId(
        userId: UUID,
        authCode: String
    ) {
        val toLoginId = verifyAndGetToLoginId(userId, authCode)
        verifyDuplicatedLoginId(toLoginId)

        val user = userRepository.findByIdOrNull(userId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        user.updateLoginId(toLoginId)
        userRepository.save(user)
    }

    private fun verifyAndGetToLoginId(
        userId: UUID,
        authCode: String
    ): String {
        val id = userId.toString()
        val loginIdChange = loginIdChangeRepository.findByIdOrNull(id)
        val toLoginId = loginIdChange?.toLoginId
        if (loginIdChange?.authCode != authCode || toLoginId == null)
            throw BusinessException(ErrorCode.LOGIN_ID_NOT_ALLOWED)

        loginIdChangeRepository.deleteById(id)

        return toLoginId
    }

    private fun verifyDuplicatedLoginId(loginId: String) {
        val user = userRepository.findByLoginId(loginId)
        if (user != null)
            throw BusinessException(ErrorCode.USER_ALREADY_REGISTERED)
    }

    // TODO: Remove
    @Transactional
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