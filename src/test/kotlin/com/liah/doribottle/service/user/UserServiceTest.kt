package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.group.GroupType.COMPANY
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.PenaltyType.DAMAGED_CUP
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class UserServiceTest : BaseServiceTest() {
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var groupRepository: GroupRepository

    @DisplayName("유저 조회")
    @Test
    fun get() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        clear()

        //when
        val result = userService.get(user.id)

        //then
        assertThat(result.loginId).isEqualTo(USER_LOGIN_ID)
        assertThat(result.name).isEqualTo("Tester 1")
        assertThat(result.phoneNumber).isEqualTo(USER_LOGIN_ID)
        assertThat(result.role).isEqualTo(Role.USER)
        assertThat(result.penalties).isEmpty()
        assertThat(result.group).isNull()
    }

    @DisplayName("유저 조회 - TC2")
    @Test
    fun getTc2() {
        //given
        val group = groupRepository.save(Group("리아", COMPANY))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        user.imposePenalty(DAMAGED_CUP, "의도적인 컵 파손")
        user.updateGroup(group)
        clear()

        //when
        val result = userService.get(user.id)

        //then
        assertThat(result.loginId).isEqualTo(USER_LOGIN_ID)
        assertThat(result.name).isEqualTo("Tester 1")
        assertThat(result.phoneNumber).isEqualTo(USER_LOGIN_ID)
        assertThat(result.role).isEqualTo(Role.USER)
        assertThat(result.penalties)
            .extracting("userId")
            .containsExactly(user.id)
        assertThat(result.penalties)
            .extracting("type")
            .containsExactly(DAMAGED_CUP)
        assertThat(result.penalties)
            .extracting("cause")
            .containsExactly("의도적인 컵 파손")
        assertThat(result.group?.name).isEqualTo("리아")
    }

    @DisplayName("유저 조회 예외")
    @Test
    fun getException() {
        val exception = assertThrows<NotFoundException> {
            userService.get(UUID.randomUUID())
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.USER_NOT_FOUND)
        assertThat(exception.message).isEqualTo(ErrorCode.USER_NOT_FOUND.message)
    }

    @DisplayName("유저 업데이트")
    @Test
    fun update() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        clear()

        userService.update(user.id, "Updated Name", "19970224", MALE)
        clear()

        val findUser = userRepository.findByIdOrNull(user.id)
        assertThat(findUser?.name).isEqualTo("Updated Name")
        assertThat(findUser?.birthDate).isEqualTo("19970224")
        assertThat(findUser?.gender).isEqualTo(MALE)
    }
}