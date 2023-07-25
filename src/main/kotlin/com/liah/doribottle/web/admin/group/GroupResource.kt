package com.liah.doribottle.web.admin.group

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.group.GroupService
import com.liah.doribottle.web.admin.group.vm.GroupRegisterRequest
import com.liah.doribottle.web.admin.group.vm.GroupSearchRequest
import com.liah.doribottle.web.admin.group.vm.GroupSearchResponse
import com.liah.doribottle.web.admin.group.vm.GroupUpdateRequest
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/admin/api/group")
class GroupResource(
    private val groupService: GroupService
) {
    @PostMapping
    fun register(
        @Valid @RequestBody request: GroupRegisterRequest
    ): UUID {
        return groupService.register(request.name!!, request.type!!)
    }

    @GetMapping
    fun getAll(
        @ParameterObject request: GroupSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<GroupSearchResponse> {
        val result = groupService.getAll(
            name = request.name,
            type = request.type,
            pageable = pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: GroupUpdateRequest
    ) {
        groupService.update(id, request.name!!, request.type!!)
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID
    ) {
        groupService.delete(id)
    }

    @PostMapping("/{id}/user/{userId}")
    fun addUser(
        @PathVariable id: UUID,
        @PathVariable userId: UUID
    ) {
        groupService.addUser(id, userId)
    }

    @DeleteMapping("/{id}/user/{userId}")
    fun removeUser(
        @PathVariable id: UUID,
        @PathVariable userId: UUID
    ) {
        groupService.removeUser(id, userId)
    }
}