package com.liah.doribottle.web.v1.me

import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.v1.me.vm.UpdateMeRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me")
class MeController(
    private val userService: UserService
) {
    @GetMapping
    fun get() = userService.get(currentUserId()!!).toMeResponse()

    @PutMapping
    fun update(
        @Valid @RequestBody request: UpdateMeRequest
    ) {
        userService.update(
            id = currentUserId()!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender
        )
    }
}