package com.liah.doribottle.web.admin.group

import com.liah.doribottle.common.error.exception.ErrorCode
import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType.COMPANY
import com.liah.doribottle.domain.group.GroupType.UNIVERSITY
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.group.vm.GroupRegisterRequest
import com.liah.doribottle.web.admin.group.vm.GroupUpdateRequest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class GroupResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/group"

    @Autowired
    private lateinit var groupRepository: GroupRepository
    @Autowired
    private lateinit var userRepository: UserRepository

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
        groupRepository.deleteAll()
    }

    @DisplayName("기관 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = GroupRegisterRequest("서울대학교", UNIVERSITY, 30)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("기관 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertGroups()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("type", "UNIVERSITY")
        params.add("page", "0")
        params.add("size", "3")

        val expectValue = listOf("대학6", "대학5", "대학4")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].name", `is`(expectValue)))
    }

    @DisplayName("기관 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 10))

        mockMvc.perform(
            get("$endPoint/${group.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("기관 정보 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 10))
        val body = GroupUpdateRequest("리아", COMPANY, 20)

        mockMvc.perform(
            put("$endPoint/${group.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)
    }

    @DisplayName("기관 삭제")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun delete() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 30))

        mockMvc.perform(
            delete("$endPoint/${group.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 추가")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun addUser() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 30))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))

        mockMvc.perform(
            post("$endPoint/${group.id}/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 제거")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun removeUser() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 30))
        val userEntity = User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER)
        userEntity.updateGroup(group)
        val user = userRepository.save(userEntity)

        mockMvc.perform(
            delete("$endPoint/${group.id}/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @DisplayName("유저 제거 예외")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun removeUserException() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY, 30))
        val user = userRepository.save(User(USER_LOGIN_ID, "Tester 1", USER_LOGIN_ID, Role.USER))

        mockMvc.perform(
            delete("$endPoint/${group.id}/user/${user.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("message", `is`(ErrorCode.GROUP_NOT_MEMBER.message)))
    }

    private fun insertGroups() {
        groupRepository.save(Group("대학1", UNIVERSITY, 30))
        groupRepository.save(Group("대학2", UNIVERSITY, 30))
        groupRepository.save(Group("대학3", UNIVERSITY, 30))
        groupRepository.save(Group("대학4", UNIVERSITY, 30))
        groupRepository.save(Group("대학5", UNIVERSITY, 30))
        groupRepository.save(Group("대학6", UNIVERSITY, 30))
    }
}