package com.liah.doribottle.service.banner

import com.liah.doribottle.domain.banner.Banner
import com.liah.doribottle.repository.banner.BannerRepository
import com.liah.doribottle.service.BaseServiceTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull

class BannerServiceTest : BaseServiceTest() {
    @Autowired
    private lateinit var bannerService: BannerService
    @Autowired
    private lateinit var bannerRepository: BannerRepository

    @DisplayName("배너 등록")
    @Test
    fun register() {
        //given, when
        val id = bannerService.register("Test", "test", 0, true, null, null)

        //then
        val findBanner = bannerRepository.findByIdOrNull(id)

        assertThat(findBanner?.title).isEqualTo("Test")
        assertThat(findBanner?.content).isEqualTo("test")
        assertThat(findBanner?.priority).isEqualTo(0)
        assertThat(findBanner?.visible).isTrue()
        assertThat(findBanner?.backgroundColor).isNull()
        assertThat(findBanner?.imageUrl).isNull()
    }

    @DisplayName("배너 수정")
    @Test
    fun update() {
        //given
        val banner = bannerRepository.save(Banner("Test", "test", 0, true, null, null))
        clear()

        //when
        bannerService.update(banner.id, "Updated", "updated", 1, false, "#000000", null)
        clear()

        //then
        val findBanner = bannerRepository.findByIdOrNull(banner.id)

        assertThat(findBanner?.title).isEqualTo("Updated")
        assertThat(findBanner?.content).isEqualTo("updated")
        assertThat(findBanner?.priority).isEqualTo(1)
        assertThat(findBanner?.visible).isFalse()
        assertThat(findBanner?.backgroundColor).isEqualTo("#000000")
        assertThat(findBanner?.imageUrl).isNull()
    }

    @DisplayName("배너 조회")
    @Test
    fun get() {
        //given
        val banner = bannerRepository.save(Banner("Test", "test", 0, true, null, null))
        clear()

        //when
        val result = bannerService.get(banner.id)

        //then
        assertThat(result.title).isEqualTo("Test")
        assertThat(result.content).isEqualTo("test")
        assertThat(result.priority).isEqualTo(0)
        assertThat(result.visible).isTrue()
        assertThat(result.backgroundColor).isNull()
        assertThat(result.imageUrl).isNull()
    }

    @DisplayName("배너 목록 조회 페이징")
    @Test
    fun getAllPaging() {
        //given
        insertBanners()
        clear()

        //when
        val result = bannerService.getAll(
            visible = true,
            pageable = Pageable.ofSize(3)
        )

        //then
        assertThat(result)
            .extracting("title")
            .containsExactly("6", "4", "3")
    }

    @DisplayName("배너 목록 조회")
    @Test
    fun getAll() {
        //given
        insertBanners()
        clear()

        //when
        val result = bannerService.getAll(
            visible = true
        )

        //then
        assertThat(result)
            .extracting("title")
            .containsExactly("6", "4", "3", "2", "1")
    }

    fun insertBanners() {
        bannerRepository.save(Banner("1", "test", 5, true, null, null))
        bannerRepository.save(Banner("2", "test", 4, true, null, null))
        bannerRepository.save(Banner("3", "test", 3, true, null, null))
        bannerRepository.save(Banner("4", "test", 2, true, null, null))
        bannerRepository.save(Banner("5", "test", 1, false, null, null))
        bannerRepository.save(Banner("6", "test", 0, true, null, null))
    }

    @DisplayName("배너 삭제")
    @Test
    fun delete() {
        //given
        val banner = bannerRepository.save(Banner("Test", "test", 0, true, null, null))
        clear()

        //when
        bannerService.delete(banner.id)
        clear()

        //then
        val findBanner = bannerRepository.findByIdOrNull(banner.id)

        assertThat(findBanner).isNull()
    }
}