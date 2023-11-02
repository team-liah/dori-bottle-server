package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.account.dto.AdminDto
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AdminService(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder
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
                role = role
            )
        )

        return admin.id
    }

    private fun verifyDuplicatedLoginId(loginId: String) {
        val admin = adminRepository.findByLoginId(loginId)
        if (admin != null)
            throw BusinessException(ErrorCode.USER_ALREADY_REGISTERED)
    }

    // TODO: TEST
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