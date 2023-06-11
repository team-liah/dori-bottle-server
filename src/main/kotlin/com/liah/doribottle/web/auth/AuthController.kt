package com.liah.doribottle.web.auth

import com.liah.doribottle.config.security.AuthService
import com.liah.doribottle.web.auth.vm.AuthRequest
import com.liah.doribottle.web.auth.vm.AuthResponse
import com.liah.doribottle.web.auth.vm.UserJoinRequest
import com.liah.doribottle.web.auth.vm.UserResponse
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService
) {
    @PostMapping("/token")
    fun authToken(
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val accessToken = authService.auth(request.loginId!!, request.password!!)
        return ResponseEntity.ok(AuthResponse(accessToken))
    }

    @PostMapping("/join")
    fun joinUser(
        @Valid @RequestBody request: UserJoinRequest
    ): ResponseEntity<UserResponse> {
        // TODO: Call join service
        return ResponseEntity.ok(UserResponse(UUID.randomUUID()))
    }
}