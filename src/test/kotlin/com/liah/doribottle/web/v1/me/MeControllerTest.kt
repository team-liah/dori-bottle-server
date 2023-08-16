package com.liah.doribottle.web.v1.me

import com.liah.doribottle.config.security.RefreshToken
import com.liah.doribottle.config.security.RefreshTokenRepository
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.notification.Alert
import com.liah.doribottle.domain.user.Gender.MALE
import com.liah.doribottle.domain.user.PenaltyType.DAMAGED_CUP
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.notification.AlertRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.v1.me.vm.ProfileUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

class MeControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/me"

    @Autowired private lateinit var userRepository: UserRepository
    @Autowired private lateinit var refreshTokenRepository: RefreshTokenRepository
    @Autowired private lateinit var groupRepository: GroupRepository
    @Autowired private lateinit var alertRepository: AlertRepository

    private lateinit var user: User
    private lateinit var userRefreshToken: RefreshToken

    @BeforeEach
    internal fun init() {
        val group = groupRepository.save(Group("리아", GroupType.COMPANY))
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
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
                .accept(MediaType.APPLICATION_JSON)
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
                .accept(MediaType.APPLICATION_JSON)
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
            get("${endPoint}/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("id", `is`(user.id.toString())))
            .andExpect(jsonPath("loginId", `is`(user.loginId)))
            .andExpect(jsonPath("name", `is`(user.name)))
            .andExpect(jsonPath("phoneNumber", `is`(user.phoneNumber)))
            .andExpect(jsonPath("invitationCode", `is`(user.invitationCode)))
            .andExpect(jsonPath("birthDate", `is`(user.birthDate)))
            .andExpect(jsonPath("gender", `is`(user.gender)))
            .andExpect(jsonPath("role", `is`(user.role.name)))
            .andExpect(jsonPath("penaltyCount", `is`(1)))
            .andExpect(jsonPath("group.name", `is`(user.group?.name)))
    }

    @DisplayName("프로필 업데이트")
    @Test
    fun updateProfile() {
        val cookie = createAccessTokenCookie(user.id, user.loginId, user.name, user.role)
        val body = ProfileUpdateRequest("Updated Name", MALE, "19970224")

        mockMvc.perform(
            put("${endPoint}/profile")
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
    }
}