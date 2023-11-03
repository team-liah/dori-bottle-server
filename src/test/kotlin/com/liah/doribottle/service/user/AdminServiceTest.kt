package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

class AdminServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var adminService: AdminService
    @Autowired
    private lateinit var adminRepository: AdminRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @DisplayName("관리자 등록")
    @Test
    fun register() {
        //given, when
        val adminId = adminService.register(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN)
        clear()

        //then
        val findAdmin = adminRepository.findByIdOrNull(adminId)

        assertThat(findAdmin?.loginId).isEqualTo(ADMIN_LOGIN_ID)
        assertThat(passwordEncoder.matches("123456", findAdmin?.loginPassword)).isTrue
        assertThat(findAdmin?.name).isEqualTo("Tester")
        assertThat(findAdmin?.role).isEqualTo(Role.ADMIN)
    }

    @DisplayName("관리자 등록 예외")
    @Test
    fun registerException() {
        //given
        adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        clear()

        //when, then
        val exception1 = assertThrows<BusinessException> {
            adminService.register(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN)
        }
        assertThat(exception1.errorCode).isEqualTo(ErrorCode.USER_ALREADY_REGISTERED)

        val exception2 = assertThrows<IllegalArgumentException> {
            adminService.register("user", "123456", "Tester", Role.USER)
        }
        assertThat(exception2.message).isEqualTo("Non Admin role is not allowed.")
    }

    @DisplayName("관리자 조회")
    @Test
    fun get() {
        //given
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        clear()

        //when
        val result = adminService.get(admin.id)

        //then
        assertThat(result.loginId).isEqualTo(ADMIN_LOGIN_ID)
        assertThat(result.name).isEqualTo("Tester")
        assertThat(result.role).isEqualTo(Role.ADMIN)
    }

    @DisplayName("관리자 목록 조회")
    @Test
    fun getAll() {
        //given
        insertAdmins()
        clear()

        //when
        val result = adminService.getAll(
            loginId = "admin",
            name = "Tester",
            role = null,
            deleted = false,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("loginId")
            .containsExactly("admin1", "admin2", "admin3")
        assertThat(result)
            .extracting("name")
            .containsExactly("Tester 1", "Tester 2", "Tester 3")
        assertThat(result)
            .extracting("role")
            .containsExactly(Role.ADMIN, Role.MACHINE_ADMIN, Role.INSTITUTION)
    }

    private fun insertAdmins() {
        adminRepository.save(Admin("admin1", "123456", "Tester 1", Role.ADMIN))
        adminRepository.save(Admin("admin2", "123456", "Tester 2", Role.MACHINE_ADMIN))
        adminRepository.save(Admin("admin3", "123456", "Tester 3", Role.INSTITUTION))
        adminRepository.save(Admin("admin4", "123456", "Tester 4", Role.MACHINE_ADMIN))
        adminRepository.save(Admin("admin5", "123456", "Tester 5", Role.ADMIN))
        adminRepository.save(Admin("admin6", "123456", "Tester 6", Role.INSTITUTION))
    }

    @DisplayName("관리자 수정")
    @Test
    fun update() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        clear()

        adminService.update(admin.id, "updated", "updated", "updated", Role.MACHINE_ADMIN)
        clear()

        val findAdmin = adminRepository.findByIdOrNull(admin.id)
        assertThat(findAdmin?.loginId).isEqualTo("updated")
        assertThat(findAdmin?.loginPassword).isEqualTo("updated")
        assertThat(findAdmin?.name).isEqualTo("updated")
        assertThat(findAdmin?.role).isEqualTo(Role.MACHINE_ADMIN)
    }

    @DisplayName("관리자 수정 예외")
    @Test
    fun updateException() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        clear()

        val exception = assertThrows<IllegalArgumentException> {
            adminService.update(admin.id, "updated", "updated", "updated", Role.USER)
        }
        assertThat(exception.message).isEqualTo("Non Admin role is not allowed.")
    }

    @DisplayName("관리자 삭제")
    @Test
    fun delete() {
        val admin = adminRepository.save(Admin(ADMIN_LOGIN_ID, "123456", "Tester", Role.ADMIN))
        clear()

        adminService.delete(admin.id)
        clear()

        val findAdmin = adminRepository.findByIdOrNull(admin.id)
        assertThat(findAdmin?.deleted).isTrue()
    }
}