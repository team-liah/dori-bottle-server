package com.liah.doribottle.web.admin.group

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.group.GroupService
import com.liah.doribottle.service.group.dto.GroupDto
import com.liah.doribottle.web.admin.group.vm.GroupRegisterRequest
import com.liah.doribottle.web.admin.group.vm.GroupSearchRequest
import com.liah.doribottle.web.admin.group.vm.GroupUpdateRequest
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
@RequestMapping("/admin/api/group")
class GroupResource(
    private val groupService: GroupService
) {
    @Operation(summary = "기관 등록")
    @PostMapping
    fun register(
        @Valid @RequestBody request: GroupRegisterRequest
    ): UUID {
        return groupService.register(
            name = request.name!!,
            type = request.type!!,
            discountRate = request.discountRate!!
        )
    }

    @Operation(summary = "기관 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: GroupSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<GroupDto> {
        val result = groupService.getAll(
            name = request.name,
            type = request.type,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "기관 조회")
    @GetMapping("/{id}")
    fun get(
        @PathVariable id: UUID
    ): GroupDto {
        return groupService.get(id)
    }

    @Operation(summary = "기관 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: GroupUpdateRequest
    ) {
        groupService.update(
            id = id,
            name = request.name!!,
            type = request.type!!,
            discountRate = request.discountRate!!
        )
    }

    @Operation(summary = "기관 삭제")
    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ) {
        groupService.delete(id)
    }

    @Operation(summary = "기관 - 유저 추가")
    @PostMapping("/{id}/user/{userId}")
    fun addUser(
        @PathVariable id: UUID,
        @PathVariable userId: UUID
    ) {
        groupService.addUser(id, userId)
    }

    @Operation(summary = "기관 - 유저 제거")
    @DeleteMapping("/{id}/user/{userId}")
    fun removeUser(
        @PathVariable id: UUID,
        @PathVariable userId: UUID
    ) {
        groupService.removeUser(id, userId)
    }
}