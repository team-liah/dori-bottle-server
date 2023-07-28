package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.AdminRefreshToken
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRefreshTokenRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.account.dto.AdminDto
import com.liah.doribottle.service.account.dto.AuthDto
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
@Transactional
class AdminAccountService(
    private val adminRepository: AdminRepository,
    private val adminRefreshTokenRepository: AdminRefreshTokenRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) {
    fun register(
        loginId: String,
        loginPassword: String,
        name: String,
        role: Role
    ): UUID {
        verifyDuplicatedLoginId(loginId)

        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val admin = adminRepository
            .save(Admin(loginId, encryptedPassword, name, Role.ADMIN))

        return admin.id
    }

    private fun verifyDuplicatedLoginId(loginId: String) {
        val admin = adminRepository.findByLoginId(loginId)
        if (admin != null)
            throw BusinessException(ErrorCode.USER_ALREADY_REGISTERED)
    }

    fun auth(
        loginId: String,
        loginPassword: String
    ): AuthDto {
        val admin = adminRepository.findByLoginId(loginId)
            ?: throw UnauthorizedException()
        verifyLoginPassword(admin, loginPassword)

        val accessToken = tokenProvider.createToken(
            admin.id,
            admin.loginId,
            admin.name,
            admin.role
        )
        val refreshToken = createRefreshToken(admin)
        return AuthDto(accessToken, refreshToken)
    }

    private fun verifyLoginPassword(
        admin: Admin,
        loginPassword: String
    ) {
        if (!passwordEncoder.matches(loginPassword, admin.loginPassword))
            throw BadCredentialsException("Invalid login password.")
    }

    fun refreshAuth(
        refreshToken: String?,
        millis: Long
    ): AuthDto {
        val validRefreshToken = adminRefreshTokenRepository
            .findByTokenAndExpiredDateIsAfter(refreshToken, Instant.now())
            ?: throw UnauthorizedException()

        validRefreshToken.refresh(millis)

        val accessToken = tokenProvider.createToken(
            validRefreshToken.admin.id,
            validRefreshToken.admin.loginId,
            validRefreshToken.admin.name,
            validRefreshToken.admin.role
        )
        val refreshedToken = validRefreshToken.token
        return AuthDto(accessToken, refreshedToken)
    }

    private fun createRefreshToken(
        admin: Admin
    ): String {
        val refreshToken = adminRefreshTokenRepository.save(AdminRefreshToken(admin))

        return refreshToken.token
    }

    // TODO: Migrate AdminService
    @Transactional(readOnly = true)
    fun get(
        loginId: String
    ): AdminDto {
        val admin = adminRepository.findByLoginId(loginId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        return admin.toDto()
    }

    //TODO: Remove
    fun createDummyAdmin(
        adminLoginId: String,
        adminLoginPassword: String,
        machineLoginId: String,
        machineLoginPassword: String,
    ) {
        val admin = adminRepository.findByLoginId(adminLoginId)
        if (admin == null) {
            register(adminLoginId, adminLoginPassword, "안감독", Role.ADMIN)
        }

        val machine = adminRepository.findByLoginId(machineLoginId)
        if (machine == null) {
            register(machineLoginId, machineLoginPassword, "MACHINE", Role.MACHINE_ADMIN)
        }
    }
}