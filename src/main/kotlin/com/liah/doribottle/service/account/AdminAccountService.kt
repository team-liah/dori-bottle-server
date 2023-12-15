package com.liah.doribottle.service.account

import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.account.dto.AuthDto
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.*

@Service
class AdminAccountService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) {
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
            role = admin.role,
            groupCode = admin.group?.code
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
            role = admin.role,
            groupCode = admin.group?.code
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
}