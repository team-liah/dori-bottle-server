package com.liah.doribottle.web.admin.user

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.user.BlockedCauseType
import com.liah.doribottle.domain.user.PenaltyType.DAMAGED_CUP
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.user.vm.UserPenaltyImposeRequest
import com.liah.doribottle.web.admin.user.vm.UserUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class UserResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/user"

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var groupRepository: GroupRepository

    private lateinit var group: Group

    @BeforeEach
    internal fun init() {
        group = groupRepository.save(Group("리아", GroupType.COMPANY, 30))
    }

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
        groupRepository.deleteAll()
    }

    @DisplayName("유저 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val userEntity = User("010-5638-3316", "Tester", "010-5638-3316", Role.USER)
        userEntity.updateGroup(group)
        userEntity.imposePenalty(DAMAGED_CUP, "의도적인 컵 파손")
        val user = userRepository.save(userEntity)

        mockMvc.perform(
            get("${endPoint}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("loginId", `is`("010-5638-3316")))
            .andExpect(jsonPath("name", `is`("Tester")))
            .andExpect(jsonPath("group.name", `is`("리아")))
            .andExpect(jsonPath("penalties[*].type", `is`(listOf(DAMAGED_CUP.name))))
    }

    @DisplayName("유저 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertUsers()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("name", "Tester")
        params.add("page", "0")
        params.add("size", "3")

        val expectLoginId = listOf("010-0000-0006", "010-0000-0005", "010-0000-0004")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].loginId", `is`(expectLoginId)))
    }

    @DisplayName("유저 목록 조회 TC2")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAllTc2() {
        insertUsers()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("groupId", "${group.id}")
        params.add("page", "0")
        params.add("size", "3")

        val expectLoginId = listOf("010-0000-0001")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].loginId", `is`(expectLoginId)))
    }

    private fun insertUsers() {
        val userEntity1 = User("010-0000-0001", "Tester 1", "010-0000-0001", Role.USER)
        userEntity1.updateGroup(group)
        userRepository.save(userEntity1)
        userRepository.save(User("010-0000-0002", "Tester 2", "010-0000-0002", Role.USER))
        userRepository.save(User("010-0000-0003", "Tester 3", "010-0000-0003", Role.USER))
        userRepository.save(User("010-0000-0004", "Tester 4", "010-0000-0004", Role.USER))
        userRepository.save(User("010-0000-0005", "Tester 5", "010-0000-0005", Role.USER))
        userRepository.save(User("010-0000-0006", "Tester 6", "010-0000-0006", Role.USER))
    }

    @DisplayName("유저 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))

        val body = UserUpdateRequest(group.id, "메모")

        mockMvc.perform(
            put("${endPoint}/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 페널티 부여")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun imposePenalty() {
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER))

        val body = UserPenaltyImposeRequest(DAMAGED_CUP, null)

        mockMvc.perform(
            post("${endPoint}/${user.id}/penalty")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 페널티 제거")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun removePenalty() {
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.imposePenalty(DAMAGED_CUP, "의도적인 컵 파손")
        val penaltyId = user.penalties.first().id
        userRepository.save(user)

        mockMvc.perform(
            delete("${endPoint}/${user.id}/penalty/${penaltyId}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 블락 사유 제거")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun unblock() {
        val user = User(USER_LOGIN_ID, "Tester", USER_LOGIN_ID, Role.USER)
        user.block(BlockedCauseType.LOST_CUP_PENALTY, null)
        userRepository.save(user)
        val blockCauseId = user.blockedCauses.first().id

        mockMvc.perform(
            delete("${endPoint}/${user.id}/block-cause/${blockCauseId}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }
}