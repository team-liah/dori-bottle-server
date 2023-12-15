package com.liah.doribottle.service.post

import com.liah.doribottle.domain.post.Post
import com.liah.doribottle.domain.post.PostType.FAQ
import com.liah.doribottle.domain.post.PostType.NOTICE
import com.liah.doribottle.domain.user.Admin
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.post.PostRepository
import com.liah.doribottle.repository.user.AdminRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class PostServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var postService: PostService
    @Autowired
    private lateinit var postRepository: PostRepository
    @Autowired
    private lateinit var adminRepository: AdminRepository

    @DisplayName("게시글 등록")
    @Test
    fun register() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null, null))
        clear()

        //when
        val id = postService.register(admin.id, NOTICE, "공지글", "공지글 내용")
        clear()

        //then
        val findPost = postRepository.findByIdOrNull(id)
        assertThat(findPost?.author).isEqualTo(admin)
        assertThat(findPost?.type).isEqualTo(NOTICE)
        assertThat(findPost?.title).isEqualTo("공지글")
        assertThat(findPost?.content).isEqualTo("공지글 내용")
    }

    @DisplayName("게시글 목록 조회")
    @Test
    fun getAll() {
        //given
        insertPosts()
        clear()

        //when
        val result = postService.getAll(
            type = NOTICE,
            keyword = "공지글",
            pageable = Pageable.ofSize(5)
        )

        assertThat(result)
            .extracting("type")
            .containsExactly(NOTICE, NOTICE, NOTICE, NOTICE, NOTICE)
        assertThat(result)
            .extracting("title")
            .containsExactly("공지글 1", "공지글 2", "공지글 3", "공지글 4", "공지글 5")
        assertThat(result)
            .extracting("content")
            .containsExactly("공지글 1 내용", "공지글 2 내용", "공지글 3 내용", "공지글 4 내용", "공지글 5 내용")
    }

    private fun insertPosts() {
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null, null))
        postRepository.save(Post(admin, NOTICE, "공지글 1", "공지글 1 내용"))
        postRepository.save(Post(admin, FAQ, "FAQ 1", "FAQ 1 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 2", "공지글 2 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 3", "공지글 3 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 4", "공지글 4 내용"))
        postRepository.save(Post(admin, NOTICE, "공지글 5", "공지글 5 내용"))
    }

    @DisplayName("게시글 조회")
    @Test
    fun get() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))
        clear()

        //when
        val result = postService.get(post.id)

        //then
        assertThat(result.author.id).isEqualTo(admin.id)
        assertThat(result.type).isEqualTo(NOTICE)
        assertThat(result.title).isEqualTo("공지글")
        assertThat(result.content).isEqualTo("공지글 내용")
    }

    @DisplayName("게시글 수정")
    @Test
    fun update() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))
        clear()

        //when
        postService.update(post.id, FAQ, "FAQ", "FAQ 내용")
        clear()

        //then
        val findPost = postRepository.findByIdOrNull(post.id)
        assertThat(findPost?.author).isEqualTo(admin)
        assertThat(findPost?.type).isEqualTo(FAQ)
        assertThat(findPost?.title).isEqualTo("FAQ")
        assertThat(findPost?.content).isEqualTo("FAQ 내용")
    }

    @DisplayName("게시글 삭제")
    @Test
    fun remove() {
        //given
        val admin = adminRepository.save(Admin("admin", "1234", "admin", Role.ADMIN, null, null, null, null))
        val post = postRepository.save(Post(admin, NOTICE, "공지글", "공지글 내용"))
        clear()

        //when
        postService.remove(post.id)
        clear()

        //then
        val findPost = postRepository.findByIdOrNull(post.id)
        assertThat(findPost).isNull()
    }
}