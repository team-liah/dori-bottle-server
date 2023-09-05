package com.liah.doribottle.web.v1.account

import com.liah.doribottle.common.error.exception.UnauthorizedException
import com.liah.doribottle.constant.ACCESS_TOKEN
import com.liah.doribottle.constant.REFRESH_TOKEN
import com.liah.doribottle.event.dummy.DummyInitEvent
import com.liah.doribottle.extension.createCookie
import com.liah.doribottle.extension.currentUser
import com.liah.doribottle.extension.currentUserLoginId
import com.liah.doribottle.extension.expireCookie
import com.liah.doribottle.service.account.AccountService
import com.liah.doribottle.service.sms.SmsService
import com.liah.doribottle.web.v1.account.vm.*
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ThreadLocalRandom

@RestController
@RequestMapping("/api/v1/account")
class AccountController(
    private val accountService: AccountService,
    private val smsService: SmsService,
    @Value("\${app.auth.jwt.expiredMs}") private val jwtExpiredMs: Long,
    @Value("\${app.auth.refreshToken.expiredMs}") private val refreshTokenExpiredMs: Long,

    // TODO: Remove
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    @Operation(summary = "인증 SMS 발송 요청")
    @PostMapping("/auth/send-sms")
    fun sendSms(
        @Valid @RequestBody request: SendSmsRequest
    ) {
        val authCode = ThreadLocalRandom.current().nextInt(100000, 999999).toString()
        accountService.updatePassword(request.loginId!!, authCode)

        smsService.sendLoginAuthSms(request.loginId, authCode)
    }

    @Operation(summary = "인증")
    @PostMapping("/auth")
    fun auth(
        httpRequest: HttpServletRequest,
        @Valid @RequestBody request: AuthRequest
    ): ResponseEntity<AuthResponse> {
        val result = accountService.auth(
            loginId = request.loginId!!,
            loginPassword = request.loginPassword!!
        )

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

    @Operation(summary = "인증 Refresh")
    @PostMapping("/refresh-auth")
    fun refreshAuth(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?
    ): ResponseEntity<AuthResponse> {
        val result = accountService.refreshAuth(refreshToken)

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

    @Operation(summary = "자판기 전용 인증 토큰 발급")
    @GetMapping("/pre-auth")
    fun preAuth() = PreAuthResponse(accountService.preAuth(currentUser()!!))

    @Operation(summary = "회원 등록")
    @PostMapping("/register")
    fun register(
        httpRequest: HttpServletRequest,
        @CookieValue("refresh_token") refreshToken: String?,
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<AuthResponse> {
        accountService.register(
            loginId = currentUserLoginId()!!,
            name = request.name!!,
            birthDate = request.birthDate!!,
            gender = request.gender,
            agreedTermsOfService = request.agreedTermsOfService!!,
            agreedTermsOfPrivacy = request.agreedTermsOfPrivacy!!,
            agreedTermsOfMarketing = request.agreedTermsOfMarketing!!
        )

        val result = try {
            accountService.refreshAuth(refreshToken)
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

    @Operation(summary = "로그아웃")
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

    //TODO: Remove
    @PostMapping("/dummy-auth")
    fun dummyAuth(
        httpRequest: HttpServletRequest
    ): ResponseEntity<AuthResponse> {
        val result = accountService.dummyAuth()

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

    //TODO: Remove
    @PostMapping("/dummy-data")
    fun dummyData() {
        applicationEventPublisher.publishEvent(
            DummyInitEvent(true)
        )
    }
}