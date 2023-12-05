package com.liah.doribottle.web.admin.user

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.service.user.dto.UserDto
import com.liah.doribottle.web.admin.user.vm.UserPenaltyImposeRequest
import com.liah.doribottle.web.admin.user.vm.UserSearchRequest
import com.liah.doribottle.web.admin.user.vm.UserUpdateRequest
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
@RequestMapping("/admin/api/user")
class UserResource(
    private val userService: UserService
) {
    @Operation(summary = "유저 조회")
    @GetMapping("/{id}")
    fun get(@PathVariable id: UUID) = userService.get(id)

    @Operation(summary = "유저 목록 조회")
    @GetMapping
    fun getAll(
        @ParameterObject request: UserSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<UserDto> {
        val result = userService.getAll(
            name = request.name,
            phoneNumber = request.phoneNumber,
            birthDate = request.birthDate,
            gender = request.gender,
            active = request.active,
            blocked = request.blocked,
            groupId = request.groupId,
            pageable = pageable
        )

        return CustomPage.of(result)
    }

    @Operation(summary = "유저 수정")
    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UserUpdateRequest
    ) {
        val user = userService.get(id)
        userService.update(
            id = id,
            name = user.name,
            birthDate = user.birthDate,
            gender = user.gender,
            description = request.description,
            groupId = request.groupId
        )
    }

    @Operation(summary = "유저 페널티 부과")
    @PostMapping("/{id}/penalty")
    fun imposePenalty(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UserPenaltyImposeRequest
    ) {
        userService.imposePenalty(
            id = id,
            penaltyType = request.penaltyType!!,
            penaltyCause = request.penaltyCause
        )
    }

    @Operation(summary = "유저 페널티 제거")
    @DeleteMapping("/{id}/penalty/{penaltyId}")
    fun removePenalty(
        @PathVariable id: UUID,
        @PathVariable penaltyId: UUID
    ) {
        userService.removePenalty(
            id = id,
            penaltyId = penaltyId
        )
    }

    @Operation(summary = "유저 블락 사유 제거")
    @DeleteMapping("/{id}/block-cause/{blockCauseId}")
    fun unblock(
        @PathVariable id: UUID,
        @PathVariable blockCauseId: UUID
    ) {
        userService.unblock(
            id = id,
            blockedCauseIds = setOf(blockCauseId)
        )
    }
}