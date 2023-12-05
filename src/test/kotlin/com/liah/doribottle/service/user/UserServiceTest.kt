package com.liah.doribottle.service.user

import com.liah.doribottle.common.error.exception.BusinessException
import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.common.error.exception.NotFoundException
import com.liah.doribottle.constant.LOST_CUP_PRICE
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType.COMPANY
import com.liah.doribottle.domain.user.BlockedCauseType.FIVE_PENALTIES
import com.liah.doribottle.domain.user.BlockedCauseType.LOST_CUP_PENALTY
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.PenaltyType.*
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.service.BaseServiceTest
import com.liah.doribottle.service.sqs.AwsSqsSender
import com.liah.doribottle.service.sqs.dto.PointSaveMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.util.*

class UserServiceTest : BaseServiceTest() {
    @Autowired private lateinit var userService: UserService
    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var groupRepository: GroupRepository

    @MockBean
    private lateinit var mockAwsSqsSender: AwsSqsSender

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
        val group = groupRepository.save(Group("리아", COMPANY, 30))
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

    @DisplayName("유저 조회 - TC3")
    @Test
    fun getTc3() {
        //given
        val group = groupRepository.save(Group("리아", COMPANY, 30))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        user.imposePenalty(DAMAGED_CUP, "의도적인 컵 파손")
        user.block(LOST_CUP_PENALTY, null)
        user.updateGroup(group)
        clear()

        //when
        val result = userService.get(user.id)

        //then
        assertThat(result.loginId).isEqualTo(USER_LOGIN_ID)
        assertThat(result.name).isEqualTo("Tester 1")
        assertThat(result.phoneNumber).isEqualTo(USER_LOGIN_ID)
        assertThat(result.role).isEqualTo(Role.USER)
        assertThat(result.group?.name).isEqualTo("리아")
        assertThat(result.penalties)
            .extracting("userId")
            .containsExactly(user.id)
        assertThat(result.penalties)
            .extracting("type")
            .containsExactly(DAMAGED_CUP)
        assertThat(result.penalties)
            .extracting("cause")
            .containsExactly("의도적인 컵 파손")
        assertThat(result.blockedCauses)
            .extracting("userId")
            .containsExactly(user.id)
        assertThat(result.blockedCauses)
            .extracting("type")
            .containsExactly(LOST_CUP_PENALTY)
        assertThat(result.blockedCauses)
            .extracting("clearPrice")
            .containsExactly(LOST_CUP_PRICE)
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
        val group = groupRepository.save(Group("리아", COMPANY, 30))
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
        val group = groupRepository.save(Group("리아", COMPANY, 30))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))
        clear()

        userService.update(user.id, "Updated Name", "19970224", MALE, group.id)
        clear()

        val findUser = userRepository.findByIdOrNull(user.id)
        assertThat(findUser?.name).isEqualTo("Updated Name")
        assertThat(findUser?.birthDate).isEqualTo("19970224")
        assertThat(findUser?.gender).isEqualTo(MALE)
        assertThat(findUser?.group?.id).isEqualTo(group.id)
    }

    @DisplayName("초대코드 등록")
    @Test
    fun registerInvitationCode() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        userRepository.save(invitee)
        clear()

        //when
        userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        clear()

        //then
        val findInviter = userRepository.findByIdOrNull(inviter.id)
        val findInvitee = userRepository.findByIdOrNull(invitee.id)

        assertThat(findInviter?.invitationCount).isEqualTo(0)
        assertThat(findInvitee?.inviterId!!).isEqualTo(findInviter?.id!!)

        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대코드 등록 TC2")
    @Test
    fun registerInvitationCodeTc2() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.use()
        userRepository.save(invitee)
        clear()

        //when
        userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        clear()

        val findInviter = userRepository.findByIdOrNull(inviter.id)
        val findInvitee = userRepository.findByIdOrNull(invitee.id)

        assertThat(findInviter?.invitationCount).isEqualTo(1)
        assertThat(findInvitee?.inviterId!!).isEqualTo(findInviter?.id!!)
        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대코드 등록 TC3")
    @Test
    fun registerInvitationCodeTc3() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        (0..3).forEach { _ -> inviter.increaseInvitationCount() } // +4
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.use()
        userRepository.save(invitee)
        clear()

        //when
        userService.registerInvitationCode(invitee.id, inviter.invitationCode)
        clear()

        val findInviter = userRepository.findByIdOrNull(inviter.id)
        val findInvitee = userRepository.findByIdOrNull(invitee.id)

        assertThat(findInviter?.invitationCount).isEqualTo(5)
        assertThat(findInvitee?.inviterId!!).isEqualTo(findInviter?.id!!)
        verify(mockAwsSqsSender, times(2)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대코드 등록 예외")
    @Test
    fun registerInvitationCodeException() {
        //given
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        userRepository.save(invitee)
        clear()

        //when, then
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
        //given
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.setInviter(inviter)
        userRepository.save(invitee)
        clear()

        //when
        userService.rewardInviterByInvitee(invitee.id)
        clear()

        //then
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInviter?.invitationCount).isEqualTo(1)
    }

    @DisplayName("초대자 보상 지급 TC2")
    @Test
    fun rewardInviterByInviteeTc2() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        (0..3).forEach { _ -> inviter.increaseInvitationCount() } // +4
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.setInviter(inviter)
        userRepository.save(invitee)
        clear()

        //when
        userService.rewardInviterByInvitee(invitee.id)
        clear()

        //then
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInviter?.invitationCount).isEqualTo(5)
        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대자 보상 지급 TC3")
    @Test
    fun rewardInviterByInviteeTc3() {
        //given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val inviter = User(USER_LOGIN_ID, "inviter", USER_LOGIN_ID, Role.USER)
        inviter.register()
        (0..8).forEach { _ -> inviter.increaseInvitationCount() } // +8
        userRepository.save(inviter)
        val invitee = User("010-0000-0001", "invitee", "010-0000-0001", Role.USER)
        invitee.register()
        invitee.setInviter(inviter)
        userRepository.save(invitee)
        clear()

        //when
        userService.rewardInviterByInvitee(invitee.id)
        clear()

        //then
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInviter?.invitationCount).isEqualTo(10)
        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("유저 페널티 부과")
    @Test
    fun imposePenalty() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        clear()

        //when
        userService.imposePenalty(user.id, DAMAGED_CUP, null)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.penalties)
            .extracting("type")
            .containsExactly(DAMAGED_CUP)
    }

    @DisplayName("유저 페널티 부과 TC2")
    @Test
    fun imposePenaltyTc2() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.imposePenalty(DAMAGED_CUP, null)
        user.imposePenalty(NON_MANNER, null)
        user.imposePenalty(ETC, null)
        user.imposePenalty(DAMAGED_CUP, null)
        userRepository.save(user)
        clear()

        //when
        userService.imposePenalty(user.id, DAMAGED_CUP, null)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.penalties)
            .extracting("type")
            .containsExactly(DAMAGED_CUP, NON_MANNER, ETC, DAMAGED_CUP, DAMAGED_CUP)

        assertThat(findUser?.blocked).isTrue()
        assertThat(findUser?.blockedCauses)
            .extracting("type")
            .containsExactly(FIVE_PENALTIES)
    }

    @DisplayName("유저 페널티 제거")
    @Test
    fun removePenalty() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.imposePenalty(DAMAGED_CUP, null)
        userRepository.save(user)
        val penaltyId = user.penalties.first().id
        clear()

        //when
        userService.removePenalty(user.id, penaltyId)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.penalties).isEmpty()
    }

    @DisplayName("유저 블락")
    @Test
    fun block() {
        //given
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))
        clear()

        //when
        userService.block(user.id, LOST_CUP_PENALTY, null)
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.blocked).isTrue()
        assertThat(findUser?.blockedCauses)
            .extracting("type")
            .containsExactly(LOST_CUP_PENALTY)
    }

    @DisplayName("유저 블락 해제")
    @Test
    fun unblock() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.block(LOST_CUP_PENALTY, "cup1 분실")
        user.block(LOST_CUP_PENALTY, "cup2 분실")
        val blockedCauseIds = user.blockedCauses.map { it.id }
        userRepository.save(user)
        clear()

        //when
        userService.unblock(user.id, blockedCauseIds.toSet())
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.blocked).isFalse()
        assertThat(findUser?.blockedCauses).isEmpty()
    }

    @DisplayName("유저 블락 해제 TC2")
    @Test
    fun unblockTc2() {
        //given
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.block(LOST_CUP_PENALTY, "cup1 분실")
        user.block(LOST_CUP_PENALTY, "cup2 분실")
        val blockedCauseId = user.blockedCauses.first().id
        userRepository.save(user)
        clear()

        //when
        userService.unblock(user.id, setOf(blockedCauseId))
        clear()

        //then
        val findUser = userRepository.findByIdOrNull(user.id)

        assertThat(findUser?.blocked).isTrue()
        assertThat(findUser?.blockedCauses)
            .extracting("type")
            .containsExactly(LOST_CUP_PENALTY)
        assertThat(findUser?.blockedCauses)
            .extracting("description")
            .containsExactly("cup2 분실")
    }
}