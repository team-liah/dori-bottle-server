package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminQueryRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.user.dto.AdminDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class AdminService(
    private val adminRepository: AdminRepository,
    private val adminQueryRepository: AdminQueryRepository,
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

    @Transactional(readOnly = true)
    fun get(
        id: UUID
    ): AdminDto {
        val admin = adminRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        return admin.toDto()
    }

    @Transactional(readOnly = true)
    fun getAll(
        loginId: String? = null,
        name: String? = null,
        role: Role? = null,
        deleted: Boolean? = null,
        pageable: Pageable
    ): Page<AdminDto> {
        return adminQueryRepository.getAll(
            loginId = loginId,
            name = name,
            role = role,
            deleted = deleted,
            pageable = pageable
        ).map { it.toDto() }
    }

    @Transactional
    fun update(
        id: UUID,
        loginId: String,
        loginPassword: String,
        name: String,
        role: Role
    ) {
        val admin = adminRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        admin.update(
            loginId = loginId,
            loginPassword = loginPassword,
            name = name,
            role = role
        )
    }

    @Transactional
    fun delete(
        id: UUID
    ) {
        val admin = adminRepository.findByIdOrNull(id)
            ?: throw NotFoundException(ErrorCode.USER_NOT_FOUND)

        admin.delete()
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