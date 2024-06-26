package com.liah.doribottle.web.v1.me

import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.notification.Alert
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.PenaltyType.*
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.messaging.AwsSqsSender
import com.liah.doribottle.messaging.vm.PointSaveMessage
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.me.vm.InvitationCodeRegisterRequest
import com.liah.doribottle.web.v1.me.vm.ProfileUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MeControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/me"

    @Autowired private lateinit var userRepository: UserRepository

    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository

    @Autowired private lateinit var groupRepository: GroupRepository

    @Autowired private lateinit var alertRepository: AlertRepository

    @MockBean
    private lateinit var mockAwsSqsSender: AwsSqsSender

    private lateinit var user: User
    private lateinit var userRefreshToken: RefreshToken

    @BeforeEach
    internal fun init() {
        val group = groupRepository.save(Group("리아", GroupType.COMPANY, 30))
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.register()
        userEntity.imposePenalty(DAMAGED_CUP, "의도적인 컵 파손")
        userEntity.updateGroup(group)
        user = userRepository.save(userEntity)
        userRefreshToken = refreshTokenRepository.save(RefreshToken(userId = user.id.toString()))
    }

    @AfterEach
    internal fun destroy() {
        refreshTokenRepository.deleteAll()
        userRepository.deleteAll()
    }

    @DisplayName("현재 로그인 유저 조회")
    @Test
    fun get() {
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("alertCount", `is`(0)))
    }

    @DisplayName("현재 로그인 유저 조회 TC2")
    @Test
    fun getTc2() {
        alertRepository.save(Alert(user.id.toString(), 7))
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("alertCount", `is`(7)))
    }

    @DisplayName("프로필 조회")
    @Test
    fun getProfile() {
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("invitationCount", `is`(user.invitationCount)))
            .andExpect(jsonPath("inviterId", `is`(user.inviterId)))
            .andExpect(jsonPath("birthDate", `is`(user.birthDate)))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("group.name", `is`(user.group?.name)))
            .andExpect(jsonPath("penalties[*].type", `is`(listOf(DAMAGED_CUP.name))))
    }

    @DisplayName("프로필 조회 - TC2")
    @Test
    fun getProfileTC2() {
        val user = User("010-0001-0001", "Blocked User", "010-0001-0001", Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("invitationCount", `is`(user.invitationCount)))
            .andExpect(jsonPath("inviterId", nullValue()))
            .andExpect(jsonPath("birthDate", nullValue()))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("group", nullValue()))
            .andExpect(jsonPath("penalties", `is`(emptyList<Any>())))
            .andExpect(jsonPath("blocked", `is`(true)))
            .andExpect(jsonPath("blockedCauses[*].clearPrice", `is`(listOf(8000, 8000))))
            .andExpect(
                jsonPath(
                    "blockedCauses[*].type",
                    `is`(listOf(BlockedCauseType.LOST_CUP_PENALTY.name, BlockedCauseType.LOST_CUP_PENALTY.name)),
                ),
            )
    }

    @DisplayName("프로필 조회 - TC3")
    @Test
    fun getProfileTC3() {
        val user = User("010-0001-0001", "Blocked User", "010-0001-0001", Role.USER)
        user.imposePenalty(DAMAGED_CUP, null)
        user.imposePenalty(DAMAGED_CUP, null)
        user.imposePenalty(NON_MANNER, null)
        user.imposePenalty(ETC, null)
        user.imposePenalty(DAMAGED_CUP, null)
        user.imposePenalty(ETC, null)
        user.blockedCauses.firstOrNull()?.let { user.unblock(it.id) }
        userRepository.save(user)
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        mockMvc.perform(
            get("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("invitationCount", `is`(user.invitationCount)))
            .andExpect(jsonPath("inviterId", nullValue()))
            .andExpect(jsonPath("birthDate", nullValue()))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("group", nullValue()))
            .andExpect(jsonPath("penalties[*].type", `is`(listOf(ETC.name))))
            .andExpect(jsonPath("penalties[*].disabled", `is`(listOf(false))))
            .andExpect(jsonPath("blocked", `is`(false)))
            .andExpect(jsonPath("blockedCauses", `is`(emptyList<Any>())))
    }

    @DisplayName("프로필 업데이트")
    @Test
    fun updateProfile() {
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = ProfileUpdateRequest("Updated Name", MALE, "19970224")

        mockMvc.perform(
            put("$endPoint/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("초대코드 등록")
    @Test
    fun registerInvitationCode() {
        // given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val inviter = User("010-0001-0001", "Inviter", "010-0001-0001", Role.USER)
        inviter.register()
        userRepository.save(inviter)
        val body = InvitationCodeRegisterRequest(inviter.invitationCode)

        // when, then
        mockMvc.perform(
            post("$endPoint/invitation-code")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)

        val findInvitee = userRepository.findByIdOrNull(user.id)
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInvitee?.inviterId).isEqualTo(inviter.id)
        assertThat(findInviter?.invitationCount).isEqualTo(1)

        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대코드 등록 TC2")
    @Test
    fun registerInvitationCodeTc2() {
        // given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val inviter = User("010-0001-0001", "Inviter", "010-0001-0001", Role.USER)
        inviter.register()
        userRepository.save(inviter)

        userRepository.save(user)
        val body = InvitationCodeRegisterRequest(inviter.invitationCode)

        // when, then
        mockMvc.perform(
            post("$endPoint/invitation-code")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)

        val findInvitee = userRepository.findByIdOrNull(user.id)
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInvitee?.inviterId).isEqualTo(inviter.id)
        assertThat(findInviter?.invitationCount).isEqualTo(1)

        verify(mockAwsSqsSender, times(1)).send(any<PointSaveMessage>())
    }

    @DisplayName("초대코드 등록 TC3")
    @Test
    fun registerInvitationCodeTc3() {
        // given
        doNothing().`when`(mockAwsSqsSender).send(any<PointSaveMessage>())
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)

        val inviter = User("010-0001-0001", "Inviter", "010-0001-0001", Role.USER)
        inviter.register()
        (0..3).forEach { _ -> inviter.increaseInvitationCount() }
        userRepository.save(inviter)

        userRepository.save(user)
        val body = InvitationCodeRegisterRequest(inviter.invitationCode)

        // when, then
        mockMvc.perform(
            post("$endPoint/invitation-code")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)

        val findInvitee = userRepository.findByIdOrNull(user.id)
        val findInviter = userRepository.findByIdOrNull(inviter.id)

        assertThat(findInvitee?.inviterId).isEqualTo(inviter.id)
        assertThat(findInviter?.invitationCount).isEqualTo(5)

        verify(mockAwsSqsSender, times(2)).send(any<PointSaveMessage>())
    }
}
