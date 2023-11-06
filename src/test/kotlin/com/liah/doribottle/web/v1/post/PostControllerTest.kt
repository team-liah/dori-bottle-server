package com.liah.doribottle.web.v1.post

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.post.Post
import com.liah.doribottle.domain.post.PostType.FAQ
import com.liah.doribottle.domain.post.PostType.NOTICE
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.post.PostRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.BaseControllerTest
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class PostControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/post"

    @Autowired
    private lateinit var postRepository: PostRepository
    @Autowired
    private lateinit var adminRepository: AdminRepository

    @AfterEach
    internal fun destroy() {
        postRepository.deleteAll()
        adminRepository.deleteAll()
    }

    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @DisplayName("게시글 목록 조회")
    @Test
    fun getAll() {
        //given
        insertPosts()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")
        params.add("type", "NOTICE")

        val expectType = listOf(NOTICE.name, NOTICE.name, NOTICE.name)
        val expectTitle = listOf("공지글 5", "공지글 4", "공지글 3")
        val expectContent = listOf("공지글 5 내용", "공지글 4 내용", "공지글 3 내용")

        //then, when
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].title", `is`(expectTitle)))
            .andExpect(jsonPath("content[*].content", `is`(expectContent)))
    }

    fun insertPosts() {
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        postRepository.save(Post(admin, NOTICE, "공지글 1", "공지글 1 내용"))
        postRepository.save(Post(admin, FAQ, "FAQ 1", "FAQ 1 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 2", "공지글 2 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 3", "공지글 3 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 4", "공지글 4 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 5", "공지글 5 내용"))
    }

    @WithMockDoriUser(loginId = "010-0000-0000", role = Role.USER)
    @DisplayName("게시글 조회")
    @Test
    fun get() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))

        //then
        mockMvc.perform(
            get("${endPoint}/${post.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("type", `is`(NOTICE.name)))
            .andExpect(jsonPath("title", `is`("공지글")))
            .andExpect(jsonPath("content", `is`("공지글 내용")))
    }
}