package com.liah.doribottle.web.admin.user

import com.liah.doribottle.common.pageable.CustomPage
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.admin.user.vm.UserSearchRequest
import com.liah.doribottle.web.admin.user.vm.UserSearchResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api/user")
class UserResource(
    private val userService: UserService
) {
    @GetMapping
    fun getAll(
        @ParameterObject request: UserSearchRequest,
        @ParameterObject @PageableDefault(sort = ["createdDate"], direction = Sort.Direction.DESC) pageable: Pageable
    ): CustomPage<UserSearchResponse> {
        val result = userService.getAll(
            name = request.name,
            phoneNumber = request.phoneNumber,
            birthDate = request.birthDate,
            gender = request.gender,
            active = request.active,
            blocked = request.blocked,
            groupId = request.groupId,
            pageable = pageable
        ).map { it.toSearchResponse() }

        return CustomPage.of(result)
    }
}