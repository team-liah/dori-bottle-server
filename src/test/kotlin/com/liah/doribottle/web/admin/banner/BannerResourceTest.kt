package com.liah.doribottle.web.admin.banner

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.banner.Banner
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.extension.convertAnyToString
import com.liah.doribottle.repository.banner.BannerRepository
import com.liah.doribottle.web.BaseControllerTest
import com.liah.doribottle.web.admin.banner.vm.BannerPatchRequest
import com.liah.doribottle.web.admin.banner.vm.BannerRegisterOrUpdateRequest
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

class BannerResourceTest : BaseControllerTest() {
    private val endPoint = "/admin/api/banner"

    @Autowired
    private lateinit var bannerRepository: BannerRepository

    @AfterEach
    internal fun destroy() {
        bannerRepository.deleteAll()
    }

    @DisplayName("배너 등록")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun register() {
        val body = BannerRegisterOrUpdateRequest("Test", "test", "test", 0, true, null, null, null, null)

        mockMvc.perform(
            post(endPoint)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("배너 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun get() {
        val banner = bannerRepository.save(Banner("Test", "test", "test", 0, true, null, null, null, null))
        mockMvc.perform(
            get("$endPoint/${banner.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("title", `is`("Test")))
            .andExpect(jsonPath("content", `is`("test")))
    }

    @DisplayName("배너 목록 조회")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun getAll() {
        insertBanners()

        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        params.add("page", "0")
        params.add("size", "3")

        val expectTitle = listOf("6", "5", "4")
        mockMvc.perform(
            get(endPoint)
                .params(params)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("content[*].title", `is`(expectTitle)))
    }

    fun insertBanners() {
        bannerRepository.save(Banner("1", "test", "test", 5, true, null, null, null))
        bannerRepository.save(Banner("2", "test", "test", 4, true, null, null, null))
        bannerRepository.save(Banner("3", "test", "test", 3, true, null, null, null))
        bannerRepository.save(Banner("4", "test", "test", 2, true, null, null, null))
        bannerRepository.save(Banner("5", "test", "test", 1, false, null, null, null))
        bannerRepository.save(Banner("6", "test", "test", 0, true, null, null, null))
    }

    @DisplayName("배너 수정")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun update() {
        val banner = bannerRepository.save(Banner("Test", "test", "test", 0, true, null, null, null))
        val body = BannerRegisterOrUpdateRequest("Updated", "test", "updated", 0, true, "#000000", null, null, null)

        mockMvc.perform(
            put("$endPoint/${banner.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("배너 패치")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun patch() {
        val banner = bannerRepository.save(Banner("Test", "test", "test", 0, true, null, null, null))
        val body = BannerPatchRequest()

        mockMvc.perform(
            patch("$endPoint/${banner.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(body.convertAnyToString()),
        )
            .andExpect(status().isOk)
    }

    @DisplayName("배너 삭제")
    @WithMockDoriUser(loginId = ADMIN_LOGIN_ID, role = Role.ADMIN)
    @Test
    fun remove() {
        val banner = bannerRepository.save(Banner("Test", "test", "test", 0, true, null, null, null))

        mockMvc.perform(
            delete("$endPoint/${banner.id}")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
    }
}
