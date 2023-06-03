package com.liah.doribottle.user

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class UserServiceTest {
    companion object {
        const val LOGIN_ID = "010-0000-0000"
        const val NAME = "Test User"
    }

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userService: UserService

    @DisplayName("일반유저_회원가입")
    @Test
    fun joinTest() {
        //when
        val id = userService.join(LOGIN_ID, NAME)

        //then
        val findUser = userRepository.findById(id).orElse(null)
        assertThat(findUser.loginId).isEqualTo(LOGIN_ID)
        assertThat(findUser.phoneNumber).isEqualTo(LOGIN_ID)
        assertThat(findUser.name).isEqualTo(NAME)
        assertThat(findUser.role).isEqualTo(Role.ROLE_USER)
        assertThat(findUser.active).isTrue
    }

    @DisplayName("일반유저_회원가입_중복예외")
    @Test
    fun joinExceptionTest() {
        //given
        userRepository.save(User(LOGIN_ID, NAME, LOGIN_ID, Role.ROLE_USER))

        //when, then
        val exception = assertThrows<IllegalArgumentException> {
            userService.join(LOGIN_ID, "Test User 2")
        }
        assertThat(exception.message).isEqualTo("이미 존재하는 회원입니다.")
    }

    @DisplayName("일반유저_조회")
    @Test
    fun findUserByLoginIdTest() {
        //given
        userRepository.save(User(LOGIN_ID, NAME, LOGIN_ID, Role.ROLE_USER))

        //when
        val findUser = userService.findUserByLoginId(LOGIN_ID)

        //then
        assertThat(findUser?.loginId).isEqualTo(LOGIN_ID)
        assertThat(findUser?.phoneNumber).isEqualTo(LOGIN_ID)
        assertThat(findUser?.name).isEqualTo(NAME)
        assertThat(findUser?.role).isEqualTo(Role.ROLE_USER)
        assertThat(findUser?.active).isTrue
    }

}