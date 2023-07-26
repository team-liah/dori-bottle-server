package com.liah.doribottle.web.admin.account

import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.extension.createCookie
import com.liah.doribottle.extension.expireCookie
import com.liah.doribottle.service.account.AdminAccountService
import com.liah.doribottle.web.admin.account.vm.AuthRequest
import com.liah.doribottle.web.admin.account.vm.AuthResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/admin/api/account")
class AdminAccountController(
    private val adminAccountService: AdminAccountService,
    @Value("\${jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long
) {
    @PostMapping("/auth")
    fun auth(
        httpRequest: HttpServletRequest,
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val result = adminAccountService
            .auth(request.loginId!!, request.loginPassword!!)

        val accessTokenCookie = createCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            url = httpRequest.requestURL.toString(),
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toAdminResponse())
    }

    @PostMapping("/refresh-auth")
    fun refreshAuth(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        val result = adminAccountService.refreshAuth(refreshToken, refreshTokenExpiredMs)

        val accessTokenCookie = createCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN,
            value = result.accessToken,
            expiredMs = jwtExpiredMs
        )
        val refreshTokenCookie = createCookie(
            url = httpRequest.requestURL.toString(),
            name = REFRESH_TOKEN,
            value = result.refreshToken,
            expiredMs = refreshTokenExpiredMs
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
            .body(result.toAdminResponse())
    }

    @PostMapping("/logout")
    fun logout(
        httpRequest: HttpServletRequest
    ): ResponseEntity<Void> {
        val expiredAccessTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = ACCESS_TOKEN
        )
        val expiredRefreshTokenCookie = expireCookie(
            url = httpRequest.requestURL.toString(),
            name = REFRESH_TOKEN
        )

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
            .build()
    }
}