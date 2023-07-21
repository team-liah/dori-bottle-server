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
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AdminAccountService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val tokenProvider: TokenProvider
) {
    fun register(
        loginId: String,
        loginPassword: String,
        name: String,
        role: Role
    ): UUID {
        checkDuplicatedLoginId(loginId)

        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val admin = adminRepository
            .save(Admin(loginId, encryptedPassword, name, Role.ADMIN))

        return admin.id
    }

    private fun checkDuplicatedLoginId(loginId: String) {
        val admin = adminRepository.findByLoginId(loginId)
        if (admin != null)
            throw BusinessException(ErrorCode.USER_ALREADY_REGISTERED)
    }

    @Transactional(readOnly = true)
    fun auth(
        loginId: String,
        loginPassword: String
    ): AuthDto {
        val admin = adminRepository.findByLoginId(loginId)
            ?: throw UnauthorizedException()
        checkLoginPassword(admin, loginPassword)

        val accessToken = tokenProvider.createToken(admin.id, admin.loginId, admin.role)
        return AuthDto(accessToken, null)
    }

    private fun checkLoginPassword(
        admin: Admin,
        loginPassword: String
    ) {
        if (!passwordEncoder.matches(loginPassword, admin.loginPassword))
            throw BadCredentialsException("Invalid login password.")
    }

    @Transactional(readOnly = true)
    fun get(
        loginId: String
    ): AdminDto {
        val admin = adminRepository.findByLoginId(loginId)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        return admin.toDto()
    }
}