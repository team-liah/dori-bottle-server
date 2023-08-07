package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.account.dto.AdminDto
import com.liah.doribottle.service.account.dto.AuthDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class AdminAccountService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) {
    @Transactional
    fun register(
        loginId: String,
        loginPassword: String,
        name: String,
        role: Role
    ): UUID {
        verifyDuplicatedLoginId(loginId)

        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val admin = adminRepository.save(
            Admin(
                loginId = loginId,
                loginPassword = encryptedPassword,
                name = name,
                role = Role.ADMIN
            )
        )

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

        val accessToken = tokenProvider.generateAccessToken(
            id = admin.id,
            loginId = admin.loginId,
            name = admin.name,
            role = admin.role
        )
        val refreshToken = tokenProvider.generateRefreshToken(
            userId = admin.id.toString()
        )

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
        refreshToken: String?
    ): AuthDto {
        val validRefreshToken = refreshToken?.let { tokenProvider.getRefreshToken(it) }
            ?: throw UnauthorizedException()
        val admin = adminRepository.findByIdOrNull(UUID.fromString(validRefreshToken.userId))
            ?: throw UnauthorizedException()

        val accessToken = tokenProvider.generateAccessToken(
            id = admin.id,
            loginId = admin.loginId,
            name = admin.name,
            role = admin.role
        )
        val newRefreshToken = refresh(
            origin = validRefreshToken.refreshToken!!,
            userId = admin.id
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