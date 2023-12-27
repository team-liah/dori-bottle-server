package com.liah.doribottle.web.admin.banner

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.banner.BannerService
import com.liah.doribottle.service.banner.dto.BannerDto
import com.liah.doribottle.web.admin.banner.vm.BannerRegisterOrUpdateRequest
import com.liah.doribottle.web.admin.banner.vm.BannerSearchRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequestMapping("/admin/api/banner")
class BannerResource(
    private val bannerService: BannerService
) {
    @Operation(summary = "배너 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: BannerRegisterOrUpdateRequest
    ): UUID {
        return bannerService.register(
            title = request.title!!,
            content = request.content!!,
            priority = request.priority!!,
            visible = request.visible!!,
            backgroundColor = request.backgroundColor,
            imageUrl = request.imageUrl,
        )
    }

    @Operation(summary = "배너 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): BannerDto {
        return bannerService.get(id)
    }

    @Operation(summary = "배너 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: BannerSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<BannerDto> {
        val result = bannerService.getAll(
            title = request.title,
            content = request.content,
            visible = request.visible,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "배너 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: BannerRegisterOrUpdateRequest
    ) {
        bannerService.update(
            id = id,
            title = request.title!!,
            content = request.content!!,
            priority = request.priority!!,
            visible = request.visible!!,
            backgroundColor = request.backgroundColor,
            imageUrl = request.imageUrl
        )
    }

    @Operation(summary = "배너 삭제")
    @DeleteMapping("/{id}")
    fun remove(
        @PathVariable id: UUID
    ) {
        bannerService.delete(id)
    }
}