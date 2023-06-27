package com.liah.doribottle.service.account

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
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
    @Autowired private lateinit var passwordEncoder: PasswordEncoder
    @Autowired private lateinit var tokenProvider: TokenProvider

    private val loginId = "010-0000-0000"

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("인증 요청")
    @Test
    fun authRequest() {
        //given
        val loginPassword = "123456"

        //when
        val id = accountService.authRequest(loginId, loginPassword)
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
        saveUser.authRequest(encryptedPassword)
        clear()

        //when
        val token = accountService.auth(loginId, loginPassword)
        clear()

        //then
        assertThat(tokenProvider.validateToken(token)).isTrue
        assertThat(tokenProvider.getUserIdFromToken(token)).isEqualTo(saveUser.id)
        assertThat(tokenProvider.getUserRoleFromToken(token)).isEqualTo("ROLE_USER")
    }

    @DisplayName("인증 실패")
    @Test
    fun authFailed() {
        //given
        val saveUser = userRepository.save(User(loginId, "Tester", loginId, Role.USER))
        val loginPassword = "123456"
        val encryptedPassword = passwordEncoder.encode(loginPassword)
        saveUser.authRequest(encryptedPassword)
        clear()

        // TODO: DisabledException, LockedException
        //when, then
        val badCredentialsException = assertThrows<BadCredentialsException> {
            accountService.auth(loginId, "000000")
        }
        assertThat(badCredentialsException.message).isEqualTo("잘못된 인증번호입니다.")
    }

    @DisplayName("회원가입")
    @Test
    fun register() {
        //given
        val saveUser = userRepository.save(User(loginId, "사용자", loginId, Role.GUEST))
        clear()

        //when
        accountService.register(saveUser.id, loginId, "Tester", 19970224, MALE)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(saveUser.id)
        assertThat(findUser?.loginId).isEqualTo(loginId)
        assertThat(findUser?.phoneNumber).isEqualTo(loginId)
        assertThat(findUser?.role).isEqualTo(Role.USER)
        assertThat(findUser?.birthDate).isEqualTo(19970224)
        assertThat(findUser?.gender).isEqualTo(MALE)
    }
}