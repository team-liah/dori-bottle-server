package com.liah.doribottle.service.account

import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder

class AdminAccountServiceTest : BaseServiceTest() {
    @Autowired private lateinit var adminAccountService: AdminAccountService
    @Autowired private lateinit var adminRepository: AdminRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var tokenProvider: TokenProvider

    @DisplayName("인증")
    @Test
    fun auth() {
        //given
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val saveAdmin = adminRepository.save(Admin(ADMIN_LOGIN_ID, encryptedPassword, "Tester", Role.ADMIN, null, null, null, null))
        clear()

        //when
        val authDto = adminAccountService.auth(ADMIN_LOGIN_ID, loginPassword)
        clear()

        //then
        assertThat(tokenProvider.validateAccessToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.extractUserIdFromAccessToken(authDto.accessToken)).isEqualTo(saveAdmin.id)
        assertThat(tokenProvider.extractUserRoleFromAccessToken(authDto.accessToken)).isEqualTo("ROLE_ADMIN")
        assertThat(authDto.refreshToken).isNotNull
    }

    @DisplayName("인증 예외")
    @Test
    fun authException() {
        //given
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        adminRepository.save(Admin(ADMIN_LOGIN_ID, encryptedPassword, "Tester", Role.ADMIN, null, null, null, null))
        clear()

        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            adminAccountService.auth(ADMIN_LOGIN_ID, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("Invalid login password.")
    }

    @DisplayName("재인증")
    @Test
    fun refreshAuth() {
        //given
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        val saveAdmin = adminRepository.save(Admin(ADMIN_LOGIN_ID, encryptedPassword, "Tester", Role.ADMIN, null, null, null, null))
        val saveRefreshToken = refreshTokenRepository.save(RefreshToken(userId = saveAdmin.id.toString()))
        clear()

        //when
        val authDto = adminAccountService.refreshAuth(saveRefreshToken.refreshToken)
        clear()

        //then
        assertThat(tokenProvider.validateAccessToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.extractUserIdFromAccessToken(authDto.accessToken)).isEqualTo(saveAdmin.id)
        assertThat(tokenProvider.extractUserRoleFromAccessToken(authDto.accessToken)).isEqualTo("ROLE_ADMIN")
        assertThat(saveRefreshToken.refreshToken).isNotEqualTo(authDto.refreshToken)
    }
}