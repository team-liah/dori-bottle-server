package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.domain.group.Group
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
import org.springframework.data.domain.Pageable
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

    @DisplayName("유저 목록 조회")
    @Test
    fun getAll() {
        //given
        insertUsers()
        clear()

        //when
        val result = userService.getAll(
            name = "Tester",
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("loginId")
            .containsExactly("010-0000-0001", "010-0000-0002", "010-0000-0003")
        assertThat(result)
            .extracting("name")
            .containsExactly("Tester 1", "Tester 2", "Tester 3")
        assertThat(result)
            .extracting("group.name")
            .containsExactly("리아", null, null)
    }

    private fun insertUsers() {
        val group = groupRepository.save(Group("리아", COMPANY))
        val userEntity1 = User("010-0000-0001", "Tester 1", "010-0000-0001", Role.USER)
        userEntity1.updateGroup(group)
        userRepository.save(userEntity1)
        userRepository.save(User("010-0000-0002", "Tester 2", "010-0000-0002", Role.USER))
        userRepository.save(User("010-0000-0003", "Tester 3", "010-0000-0003", Role.USER))
        userRepository.save(User("010-0000-0004", "Tester 4", "010-0000-0004", Role.USER))
        userRepository.save(User("010-0000-0005", "Tester 5", "010-0000-0005", Role.USER))
        userRepository.save(User("010-0000-0006", "Tester 6", "010-0000-0006", Role.USER))
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

    @DisplayName("초대코드 등록")
    @Test
    fun registerInvitationCode() {
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        userRepository.save(invitee)
        clear()

        userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        clear()

        val findInviter = userRepository.findByIdOrNull(inviter.id)
        val findInvitee = userRepository.findByIdOrNull(invitee.id)

        assertThat(findInviter?.invitationCount).isEqualTo(0)
        assertThat(findInvitee?.inviterId!!).isEqualTo(findInviter?.id!!)
    }

    @DisplayName("초대코드 등록 TC2")
    @Test
    fun registerInvitationCodeTc2() {
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.use()
        userRepository.save(invitee)
        clear()

        userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        clear()

        val findInviter = userRepository.findByIdOrNull(inviter.id)
        val findInvitee = userRepository.findByIdOrNull(invitee.id)

        assertThat(findInviter?.invitationCount).isEqualTo(1)
        assertThat(findInvitee?.inviterId!!).isEqualTo(findInviter?.id!!)
    }

    @DisplayName("초대코드 등록 예외")
    @Test
    fun registerInvitationCodeException() {
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        userRepository.save(invitee)
        clear()

        val exception1 = assertThrows<NotFoundException> {
            userService.registerInvitationCode(invitee.id, "DummyCode")
        }
        assertThat(exception1.errorCode).isEqualTo(ErrorCode.INVITER_NOT_FOUND)

        val exception2 = assertThrows<BusinessException> {
            userService.registerInvitationCode(invitee.id, invitee.invitationCode)
        }
        assertThat(exception2.errorCode).isEqualTo(ErrorCode.INVITER_NOT_ALLOWED)

        val exception3 = assertThrows<BusinessException> {
            val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
            inviter.register()
            userRepository.save(inviter)

            invitee.setInviter(inviter)
            userRepository.save(invitee)
            clear()

            userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        }
        assertThat(exception3.errorCode).isEqualTo(ErrorCode.INVITER_ALREADY_REGISTERED)
    }

    @DisplayName("초대자 보상 지급")
    @Test
    fun rewardInviterByInvitee() {
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.setInviter(inviter)
        userRepository.save(invitee)
        clear()

        userService.rewardInviterByInvitee(invitee.id)
        clear()

        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInviter?.invitationCount).isEqualTo(1)
    }
}