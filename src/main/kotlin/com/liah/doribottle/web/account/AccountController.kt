package com.liah.doribottle.web.account

import com.liah.doribottle.common.exception.UnauthorizedException
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.extension.*
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.service.user.UserService
import com.liah.doribottle.web.account.vm.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import java.util.concurrent.ThreadLocalRandom

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val userService: UserService,
    private val smsService: SmsService,
    @Value("\${jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long
) {
    @GetMapping("/simple-profile")
    fun getSimpleProfile() = currentUser()

    @GetMapping("/profile")
    fun getProfile() = userService.get(currentUserId()!!).toProfile()

    @PostMapping("/auth/send-sms")
    fun sendSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = ThreadLocalRandom.current().nextInt(100000, 999999).toString()
        accountService.updatePassword(request.loginId!!, authCode)

        smsService.sendLoginAuthSms(request.loginId, authCode)
    }

    @PostMapping("/auth")
    fun auth(
        httpRequest: HttpServletRequest,
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val result = accountService
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
            .body(result.toResponse())
    }

    @PostMapping("/refresh-auth")
    fun refreshAuth(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        val result = accountService
            .refreshAuth(currentUserLoginId()!!, refreshToken, refreshTokenExpiredMs)

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
            .body(result.toResponse())
    }

    @PostMapping("/register")
    fun register(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?,
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        accountService.register(
            loginId = currentUserLoginId()!!,
            phoneNumber = request.phoneNumber!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender!!,
            agreedTermsOfService = request.agreedTermsOfService!!,
            agreedTermsOfPrivacy = request.agreedTermsOfPrivacy!!,
            agreedTermsOfMarketing = request.agreedTermsOfMarketing!!,
        )

        val result = try {
            accountService.refreshAuth(currentUserLoginId()!!, refreshToken, refreshTokenExpiredMs)
        } catch (e: UnauthorizedException) {
            null
        }

        return if (result == null) {
            val expiredAccessTokenCookie = expireCookie(
                url = httpRequest.requestURL.toString(),
                name = ACCESS_TOKEN
            )
            val expiredRefreshTokenCookie = expireCookie(
                url = httpRequest.requestURL.toString(),
                name = REFRESH_TOKEN
            )
            ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredAccessTokenCookie.toString(), expiredRefreshTokenCookie.toString())
                .build()
        } else {
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
            ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString(), refreshTokenCookie.toString())
                .body(result.toResponse())
        }
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