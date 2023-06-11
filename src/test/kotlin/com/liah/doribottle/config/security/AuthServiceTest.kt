package com.liah.doribottle.config.security

import com.liah.doribottle.domain.user.Role.USER
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.domain.user.UserRepository
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class AuthServiceTest {
    companion object {
        const val LOGIN_ID = "010-0000-0000"
        const val NAME = "Test User"
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var authService: AuthService

    private fun clear() {
        entityManager.flush()
        entityManager.clear()
    }

    @DisplayName("일반유저 회원가입")
    @Test
    fun joinTest() {
        //when
        val id = authService.join(LOGIN_ID, NAME, USER)
        clear()x

        //then
        val findUser = userRepository.findById(id).orElse(null)
        Assertions.assertThat(findUser.loginId).isEqualTo(LOGIN_ID)
        Assertions.assertThat(findUser.phoneNumber).isEqualTo(LOGIN_ID)
        Assertions.assertThat(findUser.name).isEqualTo(NAME)
        Assertions.assertThat(findUser.role).isEqualTo(USER)
        Assertions.assertThat(findUser.active).isTrue
    }

    @DisplayName("일반유저 회원가입 예외")
    @Test
    fun joinExceptionTest() {
        //given
        userRepository.save(User(LOGIN_ID, NAME, LOGIN_ID, USER))
        clear()

        //when, then
        val exception = org.junit.jupiter.api.assertThrows<IllegalArgumentException> {
            authService.join(LOGIN_ID, "Test User 2", USER)
        }
        Assertions.assertThat(exception.message).isEqualTo("이미 존재하는 회원입니다.")
    }
}