package com.liah.doribottle.web.v1.me

import com.liah.doribottle.extension.currentUser
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.user.UserService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me")
class MeController(
    private val userService: UserService
) {
    @GetMapping
    fun get() = currentUser()

    @GetMapping("/profile")
    fun getProfile() = userService.get(currentUserId()!!).toProfile()
}