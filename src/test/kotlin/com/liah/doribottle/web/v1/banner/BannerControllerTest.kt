package com.liah.doribottle.web.v1.banner

import com.liah.doribottle.config.security.WithMockDoriUser
import com.liah.doribottle.domain.banner.Banner
import com.liah.doribottle.domain.user.Role
import com.liah.doribottle.repository.banner.BannerRepository
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

class BannerControllerTest : BaseControllerTest() {
    private val endPoint = "/api/v1/banner"

    @Autowired
    private lateinit var bannerRepository: BannerRepository

    @AfterEach
    internal fun destroy() {
        bannerRepository.deleteAll()
    }

    @DisplayName("배너 목록 조회")
    @WithMockDoriUser(loginId = USER_LOGIN_ID, role = Role.USER)
    @Test
    fun getAll() {
        insertBanners()

        val expectTitle = listOf("test", "test", "test", "test", "test")
        mockMvc.perform(
            get("$endPoint/all")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("[*].header", `is`(expectTitle)))
    }

    fun insertBanners() {
        bannerRepository.save(Banner("1", "test", "test", 5, true, null, null, null))
        bannerRepository.save(Banner("2", "test", "test", 4, true, null, null, null))
        bannerRepository.save(Banner("3", "test", "test", 3, true, null, null, null))
        bannerRepository.save(Banner("4", "test", "test", 2, true, null, null, null))
        bannerRepository.save(Banner("5", "test", "test", 1, false, null, null, null))
        bannerRepository.save(Banner("6", "test", "test", 0, true, null, null, null))
    }
}