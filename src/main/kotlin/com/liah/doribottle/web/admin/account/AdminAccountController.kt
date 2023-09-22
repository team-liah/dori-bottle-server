package com.liah.doribottle.web.admin.account

import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.extension.createCookie
import com.liah.doribottle.extension.expireCookie
import com.liah.doribottle.service.account.AdminAccountService
import com.liah.doribottle.web.admin.account.vm.AuthRequest
import com.liah.doribottle.web.admin.account.vm.AuthResponse
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/api/account")
class AdminAccountController(
    private val adminAccountService: AdminAccountService,
    @Value("\${app.auth.jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.auth.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long
) {
    @Operation(summary = "인증")
    @PostMapping("/auth")
    fun auth(
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val result = adminAccountService
            .auth(request.loginId!!, request.loginPassword!!)

        val accessTokenCookie = createCookie(
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toAdminResponse())
    }

    @Operation(summary = "인증 Refrest")
    @PostMapping("/refresh-auth")
    fun refreshAuth(
        @CookieValue("refresh_token") refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        val result = adminAccountService.refreshAuth(refreshToken)

        val accessTokenCookie = createCookie(
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toAdminResponse())
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    fun logout(): ResponseEntity<Void> {
        val expiredAccessTokenCookie = expireCookie(ACCESS_TOKEN)
        val expiredRefreshTokenCookie = expireCookie(REFRESH_TOKEN)
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
            .build()
    }
}