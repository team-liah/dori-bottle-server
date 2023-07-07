package com.liah.doribottle.service.account

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.*
import com.liah.doribottle.domain.user.Gender.MALE
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
import java.time.Instant

@SpringBootTest
@Transactional
class AccountServiceTest {
    @PersistenceContext private lateinit var entityManager: EntityManager
    @Autowired private lateinit var accountService: AccountService
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var tokenProvider: TokenProvider

    private val loginId = "010-0000-0000"

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("비밀번호 업데이트")
    @Test
    fun updatePassword() {
        //given
        val loginPassword = "123456"

        //when
        val id = accountService.updatePassword(loginId, loginPassword)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(id)
        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(findUser?.phoneNumber).isEqualTo(loginId)
        assertThat(passwordEncoder.matches(loginPassword, findUser?.loginPassword)).isTrue
        assertThat(findUser?.role).isEqualTo(Role.GUEST)
        assertThat(findUser?.loginExpirationDate).isAfter(Instant.now())
    }

    @DisplayName("인증")
    @Test
    fun auth() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        saveUser.updatePassword(encryptedPassword)
        clear()

        //when
        val authDto = accountService.auth(loginId, loginPassword)
        clear()

        //then
        assertThat(tokenProvider.validateToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.getUserIdFromToken(authDto.accessToken)).isEqualTo(saveUser.id)
        assertThat(tokenProvider.getUserRoleFromToken(authDto.accessToken)).isEqualTo("ROLE_USER")
        assertThat(authDto.refreshToken).isNotNull
    }

    @DisplayName("인증 예외")
    @Test
    fun authException() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        saveUser.updatePassword(encryptedPassword)
        clear()

        // TODO: DisabledException, LockedException
        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            accountService.auth(loginId, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("Invalid login password.")
    }

    @DisplayName("재인증")
    @Test
    fun refreshAuth() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val saveRefreshToken = refreshTokenRepository.save(RefreshToken(saveUser))
        clear()

        //when
        val authDto = accountService.refreshAuth(saveUser.loginId, saveRefreshToken.token, 1209600000)
        clear()

        //then
        assertThat(tokenProvider.validateToken(authDto.accessToken)).isTrue
        assertThat(tokenProvider.getUserIdFromToken(authDto.accessToken)).isEqualTo(saveUser.id)
        assertThat(tokenProvider.getUserRoleFromToken(authDto.accessToken)).isEqualTo("ROLE_USER")
        assertThat(saveRefreshToken.token).isNotEqualTo(authDto.refreshToken)
    }

    @DisplayName("회원가입")
    @Test
    fun register() {
        //given
        val saveUser = userRepository.save(User(loginId, "사용자", loginId, Role.GUEST))
        clear()

        //when
        accountService.register(saveUser.loginId, loginId, "Tester", "19970224", MALE, true, true, false)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(saveUser.id)
        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(findUser?.phoneNumber).isEqualTo(loginId)
        assertThat(findUser?.role).isEqualTo(Role.USER)
        assertThat(findUser?.birthDate).isEqualTo("19970224")
        assertThat(findUser?.gender).isEqualTo(MALE)
        assertThat(findUser?.agreedTermsOfServiceDate).isNotNull
        assertThat(findUser?.agreedTermsOfServiceDate).isNotNull
        assertThat(findUser?.agreedTermsOfMarketingDate).isNull()
    }
}