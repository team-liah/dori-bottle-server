package com.liah.doribottle.web.v1.me

import com.liah.doribottle.extension.currentUser
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.notification.NotificationService
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.v1.me.vm.MeResponse
import com.liah.doribottle.web.v1.me.vm.UpdateProfileRequest
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/me")
class MeController(
    private val userService: UserService,
    private val notificationService: NotificationService
) {
    @GetMapping
    fun get(): MeResponse {
        val doriUser = currentUser()!!
        return MeResponse.of(doriUser, notificationService.getAlertCount(doriUser.id))
    }

    @GetMapping("/profile")
    fun getProfile() = userService.get(currentUserId()!!).toProfileResponse()

    @PutMapping("/profile")
    fun updateProfile(
        @Valid @RequestBody request: UpdateProfileRequest
    ) {
        userService.update(
            id = currentUserId()!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender
        )
    }
}