package com.liah.doribottle.service.user

import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder

class AdminServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var adminService: AdminService
    @Autowired
    private lateinit var adminRepository: AdminRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val loginId = "liah"

    @DisplayName("관리자 등록")
    @Test
    fun register() {
        //given, when
        val adminId = adminService.register(loginId, "123456", "Tester", Role.ADMIN)
        clear()

        //then
        val findUser = adminRepository.findByIdOrNull(adminId)

        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(passwordEncoder.matches("123456", findUser?.loginPassword)).isTrue
        assertThat(findUser?.name).isEqualTo("Tester")
        assertThat(findUser?.role).isEqualTo(Role.ADMIN)
    }
}