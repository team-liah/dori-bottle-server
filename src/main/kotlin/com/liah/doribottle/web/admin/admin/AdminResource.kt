package com.liah.doribottle.web.admin.admin

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.user.AdminService
import com.liah.doribottle.service.user.dto.AdminDto
import com.liah.doribottle.web.admin.admin.vm.AdminPasswordUpdateRequest
import com.liah.doribottle.web.admin.admin.vm.AdminRegisterRequest
import com.liah.doribottle.web.admin.admin.vm.AdminSearchRequest
import com.liah.doribottle.web.admin.admin.vm.AdminUpdateRequest
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/admin")
class AdminResource(
    private val adminService: AdminService
) {
    @Operation(summary = "관리자 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: AdminRegisterRequest
    ): UUID {
        return adminService.register(
            loginId = request.loginId!!,
            loginPassword = request.loginPassword!!,
            name = request.name!!,
            role = request.role!!,
            email = request.email,
            phoneNumber = request.phoneNumber,
            description = request.description
        )
    }

    @Operation(summary = "관리자 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): AdminDto {
        return adminService.get(id)
    }

    @Operation(summary = "관리자 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: AdminSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<AdminDto> {
        val result = adminService.getAll(
            loginId = request.loginId,
            name = request.name,
            role = request.role,
            deleted = false,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "관리자 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AdminUpdateRequest
    ) {
        adminService.update(
            id = id,
            loginId = request.loginId!!,
            name = request.name!!,
            role = request.role!!,
            email = request.email,
            phoneNumber = request.phoneNumber,
            description = request.description
        )
    }

    @Operation(summary = "관리자 비밀번호 변경")
    @PutMapping("/{id}/password")
    fun updatePassword(
        @PathVariable id: UUID,
        @Valid @RequestBody request: AdminPasswordUpdateRequest
    ) {
        adminService.updatePassword(
            id = id,
            loginPassword = request.loginPassword!!
        )
    }

    @Operation(summary = "관리자 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ) {
        adminService.delete(id)
    }
}