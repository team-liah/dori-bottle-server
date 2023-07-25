package com.liah.doribottle.web.admin.user

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.group.Group
import com.liah.doribottle.domain.group.GroupType
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.domain.user.User
import com.liah.doribottle.repository.group.GroupRepository
import com.liah.doribottle.repository.user.UserRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
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
        group = groupRepository.save(Group("리아", GroupType.COMPANY))
    }

    @AfterEach
    internal fun destroy() {
        userRepository.deleteAll()
    }

    @DisplayName("유저 조회")
    @WithMockDoriUser(loginId = MACHINE_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun getAll() {
        // TODO: Lazy loading not working
        insertUsers()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("name", "Tester")
        params.add("page", "0")
        params.add("size", "3")

        val expectLoginId = listOf("010-0000-0006", "010-0000-0005", "010-0000-0004")
        mockMvc.perform(
            MockMvcRequestBuilders.get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].loginId", `is`(expectLoginId)))
    }

    @DisplayName("유저 조회 TC2")
    @WithMockDoriUser(loginId = MACHINE_LOGIN_ID, role = Role.MACHINE_ADMIN)
    @Test
    fun getAllTc2() {
        // TODO: Lazy loading not working
        insertUsers()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("groupId", "${group.id}")
        params.add("page", "0")
        params.add("size", "3")

        val expectLoginId = listOf("010-0000-0001")
        mockMvc.perform(
            MockMvcRequestBuilders.get(endPoint)
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
}