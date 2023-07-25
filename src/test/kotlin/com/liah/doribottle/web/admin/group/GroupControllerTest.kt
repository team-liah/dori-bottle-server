package com.liah.doribottle.web.admin.group

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType.COMPANY
import com.liah.doribottle.domain.group.GroupType.UNIVERSITY
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertJsonToString
import com.liah.doribottle.repository.group.GroupRepository
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

class GroupControllerTest : BaseControllerTest() {
    private val endPoint = "/admin/api/group"

    @Autowired
    private lateinit var groupRepository: GroupRepository

    @AfterEach
    internal fun destroy() {
        groupRepository.deleteAll()
    }

    @DisplayName("기관 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = GroupRegisterRequest("서울대학교", UNIVERSITY)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
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

    @DisplayName("기관 정보 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val group = groupRepository.save(Group("대학1", UNIVERSITY))
        val body = GroupUpdateRequest("리아", COMPANY)

        mockMvc.perform(
            put("$endPoint/${group.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertJsonToString())
        )
            .andExpect(status().isOk)
    }

    private fun insertGroups() {
        groupRepository.save(Group("대학1", UNIVERSITY))
        groupRepository.save(Group("대학2", UNIVERSITY))
        groupRepository.save(Group("대학3", UNIVERSITY))
        groupRepository.save(Group("대학4", UNIVERSITY))
        groupRepository.save(Group("대학5", UNIVERSITY))
        groupRepository.save(Group("대학6", UNIVERSITY))
    }
}