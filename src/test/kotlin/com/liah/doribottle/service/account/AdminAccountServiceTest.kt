package com.liah.doribottle.service.account

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.repository.user.RefreshTokenRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class AdminAccountServiceTest {
    @PersistenceContext private lateinit var entityManager: EntityManager
    @Autowired private lateinit var adminAccountService: AdminAccountService
    @Autowired private lateinit var adminRepository: AdminRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var tokenProvider: TokenProvider

    private val loginId = "liah"

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        //given
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val saveAdmin = adminRepository.save(Admin(loginId, encryptedPassword, "Tester", Role.ADMIN))
        clear()

        //when
        val authDto = adminAccountService.auth(loginId, loginPassword)
        clear()

        //then
        assertThat(tokenProvider.validateToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.getUserIdFromToken(authDto.accessToken)).isEqualTo(saveAdmin.id)
        assertThat(tokenProvider.getUserRoleFromToken(authDto.accessToken)).isEqualTo("ROLE_ADMIN")
    }

    @DisplayName("인증 예외")
    @Test
    fun authException() {
        //given
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        adminRepository.save(Admin(loginId, encryptedPassword, "Tester", Role.ADMIN))
        clear()

        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            adminAccountService.auth(loginId, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("Invalid login password.")
    }

    @DisplayName("관리자 등록")
    @Test
    fun register() {
        //given, when
        val adminId = adminAccountService.register(loginId, "123456", "Tester", Role.ADMIN)
        clear()

        //then
        val findUser = adminRepository.findByIdOrNull(adminId)

        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(passwordEncoder.matches("123456", findUser?.loginPassword)).isTrue
        assertThat(findUser?.name).isEqualTo("Tester")
        assertThat(findUser?.role).isEqualTo(Role.ADMIN)
    }
}