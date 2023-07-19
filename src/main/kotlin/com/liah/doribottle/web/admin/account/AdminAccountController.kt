package com.liah.doribottle.web.admin.account

import com.liah.doribottle.constant.ACCESS_TOKEN
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/admin/api/account")
class AdminAccountController(
    private val adminAccountService: AdminAccountService,
    @Value("\${jwt.expiredMs}") private val jwtExpiredMs: Long
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

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
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

        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString())
            .build()
    }
}