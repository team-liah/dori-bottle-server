package com.liah.doribottle.web.v1.banner

import com.liah.doribottle.service.banner.BannerService
import com.liah.doribottle.web.v1.banner.vm.BannerSearchResponse
import io.swagger.v3.oas.annotations.Operation
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/banner")
class BannerController(
    private val bannerService: BannerService
) {
    @Operation(summary = "배너 전체 조회")
    @GetMapping("/all")
    fun getAll(): List<BannerSearchResponse> {
        return bannerService.getAll(
            visible = true,
            pageable = Pageable.unpaged(Sort.by(Sort.Direction.DESC, "priority"))
        ).map { it.toSearchResponse() }.content
    }
}