package com.liah.doribottle.web.me

import com.liah.doribottle.config.security.TokenProvider
import com.liah.doribottle.extension.currentUser
import com.liah.doribottle.extension.currentUserId
import com.liah.doribottle.service.point.PointService
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.me.vm.PreAuthResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/me")
class MeController(
    private val userService: UserService,
    private val pointService: PointService,
    private val tokenProvider: TokenProvider
) {
    @GetMapping
    fun get() = currentUser()

    @GetMapping("/pre-auth-token")
    fun getPreAuthToken() = PreAuthResponse(tokenProvider.createPreAuthToken(currentUser()!!))

    @GetMapping("/profile")
    fun getProfile() = userService.get(currentUserId()!!).toProfile()

    @GetMapping("/remain-point")
    fun getRemainPoint() = pointService.getSum(currentUserId()!!).toRemainPoint()
}