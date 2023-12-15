package com.liah.doribottle.web.admin.post

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.post.Post
import com.liah.doribottle.domain.post.PostType.FAQ
import com.liah.doribottle.domain.post.PostType.NOTICE
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.post.PostRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.post.vm.PostRegisterOrUpdateRequest
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

class PostResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/post"

    @Autowired
    private lateinit var postRepository: PostRepository
    @Autowired
    private lateinit var adminRepository: AdminRepository

    @AfterEach
    internal fun destroy() {
        postRepository.deleteAll()
        adminRepository.deleteAll()
    }

    @DisplayName("게시글 등록")
    @Test
    fun register() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        val cookie = createAccessTokenCookie(admin.id, admin.loginId, admin.name, admin.role, null)
        val body = PostRegisterOrUpdateRequest(NOTICE, "공지글", "공지글 내용")

        mockMvc.perform(
            post(endPoint)
                .cookie(cookie)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        val findPost = postRepository.findAll().firstOrNull()
        assertThat(findPost?.author?.id).isEqualTo(admin.id)
        assertThat(findPost?.type).isEqualTo(NOTICE)
        assertThat(findPost?.title).isEqualTo("공지글")
        assertThat(findPost?.content).isEqualTo("공지글 내용")
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("게시글 목록 조회")
    @Test
    fun getAll() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        insertPosts(admin)

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")
        params.add("type", "NOTICE")

        val expectAuthorId = listOf(admin.id.toString(), admin.id.toString(), admin.id.toString())
        val expectAuthorName = listOf(admin.name, admin.name, admin.name)
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
            .andExpect(jsonPath("content[*].author.id", `is`(expectAuthorId)))
            .andExpect(jsonPath("content[*].author.name", `is`(expectAuthorName)))
            .andExpect(jsonPath("content[*].type", `is`(expectType)))
            .andExpect(jsonPath("content[*].title", `is`(expectTitle)))
            .andExpect(jsonPath("content[*].content", `is`(expectContent)))
    }

    fun insertPosts(admin: Admin) {
        postRepository.save(Post(admin, NOTICE, "공지글 1", "공지글 1 내용"))
        postRepository.save(Post(admin, FAQ, "FAQ 1", "FAQ 1 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 2", "공지글 2 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 3", "공지글 3 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 4", "공지글 4 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 5", "공지글 5 내용"))
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
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
            .andExpect(jsonPath("author.id", `is`(admin.id.toString())))
            .andExpect(jsonPath("author.name", `is`(admin.name)))
            .andExpect(jsonPath("type", `is`(NOTICE.name)))
            .andExpect(jsonPath("title", `is`("공지글")))
            .andExpect(jsonPath("content", `is`("공지글 내용")))
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("게시글 수정")
    @Test
    fun update() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))
        val body = PostRegisterOrUpdateRequest(FAQ, "FAQ", "FAQ 내용")

        mockMvc.perform(
            put("${endPoint}/${post.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString())
        )
            .andExpect(status().isOk)

        val findPost = postRepository.findByIdOrNull(post.id)
        assertThat(findPost?.author?.id).isEqualTo(admin.id)
        assertThat(findPost?.type).isEqualTo(FAQ)
        assertThat(findPost?.title).isEqualTo("FAQ")
        assertThat(findPost?.content).isEqualTo("FAQ 내용")
    }

    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @DisplayName("게시글 삭제")
    @Test
    fun remove() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))

        mockMvc.perform(
            delete("${endPoint}/${post.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)

        val findPost = postRepository.findByIdOrNull(post.id)
        assertThat(findPost).isNull()
    }
}